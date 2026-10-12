package com.courtneythan.aboutme.core.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

internal class AssetProfileSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : BundledProfileSource {
    override suspend fun read(): String =
        context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }

    companion object {
        const val ASSET_NAME = "profile.json"
    }
}

internal class FileProfileCache @Inject constructor(
    @ApplicationContext context: Context,
) : ProfileCache {
    private val file = File(context.filesDir, "profile_cache.json")

    override suspend fun read(): String? = file.takeIf { it.exists() }?.readText()

    override suspend fun write(json: String) {
        // Write to a temp file, then rename, so a crash mid-write can't leave half a file.
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.writeText(json)
        if (!tmp.renameTo(file)) {
            file.writeText(json)
            tmp.delete()
        }
    }
}

internal class OkHttpRemoteProfileSource @Inject constructor(
    private val client: OkHttpClient,
    @Named(PROFILE_URL) private val url: String,
) : RemoteProfileSource {
    override suspend fun fetch(): String? {
        if (url.isBlank()) return null
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            return response.body?.string()
        }
    }
}

/** Qualifier name for the remote profile URL, provided by the app module. */
const val PROFILE_URL = "profileUrl"
