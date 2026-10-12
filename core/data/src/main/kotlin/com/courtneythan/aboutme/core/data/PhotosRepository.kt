package com.courtneythan.aboutme.core.data

import com.courtneythan.aboutme.core.model.Photo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

interface PhotosRepository {
    /** Searches for [count] photos matching [query]. Throws [MissingApiKeyException] or [IOException]. */
    suspend fun photos(query: String, count: Int): List<Photo>

    /**
     * Fetches specific photos by Pexels ID, in the given order. Photos that fail to load
     * are skipped; it throws only if none load (or [MissingApiKeyException]).
     */
    suspend fun photosById(ids: List<Long>): List<Photo>
}

/** No Pexels key was configured (see PEXELS_API_KEY in app/build.gradle.kts). */
class MissingApiKeyException : IllegalStateException("No Pexels API key. Add PEXELS_API_KEY to local.properties.")

/** Qualifier names for values the app module provides. */
const val PEXELS_API_KEY = "pexelsApiKey"
const val PEXELS_BASE_URL = "pexelsBaseUrl"

/**
 * Searches the Pexels REST API (https://www.pexels.com/api/documentation/).
 *
 * Results are kept in memory per query, so switching tabs or rotating doesn't spend
 * the hourly request limit. The images themselves are cached on disk by Coil.
 */
@Singleton
internal class PexelsPhotosRepository @Inject constructor(
    private val client: OkHttpClient,
    @Named(PEXELS_API_KEY) private val apiKey: String,
    @Named(PEXELS_BASE_URL) private val baseUrl: String,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PhotosRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val cache = ConcurrentHashMap<String, List<Photo>>()

    override suspend fun photos(query: String, count: Int): List<Photo> {
        if (apiKey.isBlank()) throw MissingApiKeyException()
        val cacheKey = "$query|$count"
        cache[cacheKey]?.let { return it }

        val url = baseUrl.toHttpUrl().newBuilder()
            .addPathSegments("v1/search")
            .addQueryParameter("query", query)
            .addQueryParameter("per_page", count.coerceIn(1, 80).toString())
            .addQueryParameter("orientation", "landscape")
            .build()
        val body = get(url)
        val photos = json.decodeFromString(SearchResponse.serializer(), body).photos.map { it.toPhoto() }
        cache[cacheKey] = photos
        return photos
    }

    override suspend fun photosById(ids: List<Long>): List<Photo> {
        if (apiKey.isBlank()) throw MissingApiKeyException()
        // One request per photo, all in parallel. A missing or failed photo is skipped
        // so one bad ID doesn't hide the rest.
        val results = coroutineScope {
            ids.map { id -> async { runCatching { photoById(id) } } }.awaitAll()
        }
        results.forEach { result ->
            (result.exceptionOrNull() as? CancellationException)?.let { throw it }
        }
        val photos = results.mapNotNull { it.getOrNull() }
        if (photos.isEmpty() && ids.isNotEmpty()) {
            throw results.firstNotNullOfOrNull { it.exceptionOrNull() } as? IOException
                ?: IOException("None of the Pexels photos could be loaded")
        }
        return photos
    }

    private val photoCache = ConcurrentHashMap<Long, Photo>()

    private suspend fun photoById(id: Long): Photo {
        photoCache[id]?.let { return it }
        val url = baseUrl.toHttpUrl().newBuilder().addPathSegments("v1/photos/$id").build()
        val body = get(url)
        val photo = json.decodeFromString(PhotoDto.serializer(), body).toPhoto()
        photoCache[id] = photo
        return photo
    }

    private suspend fun get(url: HttpUrl): String = withContext(ioDispatcher) {
        // Pexels expects the bare key in Authorization, no "Bearer" prefix.
        val request = Request.Builder().url(url).header("Authorization", apiKey).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Pexels HTTP ${response.code}")
            response.body?.string() ?: throw IOException("Pexels returned an empty body")
        }
    }

    // ── Pexels response shapes (only the fields the app uses) ──

    @Serializable
    private data class SearchResponse(val photos: List<PhotoDto> = emptyList())

    @Serializable
    private data class PhotoDto(
        val id: Long,
        val url: String,
        val photographer: String,
        @SerialName("photographer_url") val photographerUrl: String,
        @SerialName("avg_color") val avgColor: String? = null,
        val alt: String = "",
        val src: Src,
    ) {
        fun toPhoto() = Photo(
            id = id,
            imageUrl = src.large,
            pageUrl = url,
            photographer = photographer,
            photographerUrl = photographerUrl,
            alt = alt.ifBlank { "Photo by $photographer" },
            averageColor = avgColor,
        )
    }

    @Serializable
    private data class Src(val large: String)
}
