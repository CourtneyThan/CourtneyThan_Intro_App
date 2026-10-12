package com.courtneythan.aboutme.core.data

import com.courtneythan.aboutme.core.model.Profile
import com.courtneythan.aboutme.core.model.ProfileParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Offline-first, stale-while-revalidate:
 *
 *   1. emit cache (or bundled, if the cache is missing or corrupt)
 *   2. fetch remote
 *   3. if remote parses AND differs → write cache, emit again
 *
 * Only JSON that parses successfully is ever cached, so a bad deploy of
 * profile.json can't brick the app.
 */
class OfflineFirstProfileRepository @Inject constructor(
    private val bundled: BundledProfileSource,
    private val cache: ProfileCache,
    private val remote: RemoteProfileSource,
    private val parser: ProfileParser,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ProfileRepository {

    override fun profile(): Flow<ProfileSnapshot> = flow {
        val local = cache.read()?.parseOrNull()?.let { ProfileSnapshot(it, ContentSource.CACHE) }
            ?: ProfileSnapshot(parser.parse(bundled.read()), ContentSource.BUNDLED)
        emit(local)

        val remoteJson = fetchRemoteOrNull() ?: return@flow
        val remoteProfile = remoteJson.parseOrNull() ?: return@flow
        cache.write(remoteJson)
        if (remoteProfile != local.profile) {
            emit(ProfileSnapshot(remoteProfile, ContentSource.REMOTE))
        }
    }.flowOn(ioDispatcher)

    private suspend fun fetchRemoteOrNull(): String? =
        try {
            remote.fetch()
        } catch (e: CancellationException) {
            throw e // never swallow cancellation
        } catch (e: Exception) {
            null
        }

    private fun String.parseOrNull(): Profile? =
        try {
            parser.parse(this)
        } catch (e: Exception) {
            null
        }
}
