package com.courtneythan.aboutme.core.data

import com.courtneythan.aboutme.core.model.Profile
import kotlinx.coroutines.flow.Flow

/** Where the content currently on screen came from. Shown as a tiny badge in the UI. */
enum class ContentSource { BUNDLED, CACHE, REMOTE }

data class ProfileSnapshot(
    val profile: Profile,
    val source: ContentSource,
)

interface ProfileRepository {
    /**
     * Emits local content immediately (cache, else the copy bundled in the APK), then
     * emits again only if the network returns something different.
     * Network failures are swallowed: the user keeps seeing local content.
     */
    fun profile(): Flow<ProfileSnapshot>
}
