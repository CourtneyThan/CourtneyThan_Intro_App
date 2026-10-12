package com.courtneythan.aboutme.core.data

/** The JSON shipped inside the APK. Always present, so the app works offline on first launch. */
fun interface BundledProfileSource {
    suspend fun read(): String
}

/** Last good JSON downloaded from the network. */
interface ProfileCache {
    suspend fun read(): String?
    suspend fun write(json: String)
}

/** Fetches the latest JSON, or returns null when no remote is configured. Throws on network errors. */
fun interface RemoteProfileSource {
    suspend fun fetch(): String?
}
