package com.courtneythan.aboutme.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class UnsupportedSchemaException(val version: Int) :
    IllegalStateException("Schema version $version is newer than supported ${ProfileParser.SUPPORTED_SCHEMA_VERSION}")

/**
 * Turns profile JSON into a [Profile].
 *
 * Resilience rules (see docs/adr/0002-section-fallbacks.md):
 *  - Unknown keys are ignored, so the server can add fields freely.
 *  - Each section is decoded on its own. An unknown or malformed section becomes
 *    [Section.Unsupported] instead of failing the whole document.
 *  - A broken welcome block is dropped (the app just opens on the first tab).
 *  - A schema version newer than the app understands is rejected, so the caller
 *    can keep showing the last good content.
 */
class ProfileParser(
    private val json: Json = DefaultJson,
) {
    @Serializable
    private data class RawTab(
        val id: String,
        val label: String,
        val icon: String = "star",
        val sections: List<JsonObject> = emptyList(),
    )

    @Serializable
    private data class RawProfile(
        val schemaVersion: Int = 1,
        val welcome: JsonObject? = null,
        val tabs: List<RawTab>,
    )

    fun parse(text: String): Profile {
        val raw = json.decodeFromString(RawProfile.serializer(), text)
        if (raw.schemaVersion > SUPPORTED_SCHEMA_VERSION) {
            throw UnsupportedSchemaException(raw.schemaVersion)
        }
        return Profile(
            schemaVersion = raw.schemaVersion,
            welcome = raw.welcome?.let { decodeOrNull { json.decodeFromJsonElement(Welcome.serializer(), it) } },
            tabs = raw.tabs.map { tab ->
                Tab(
                    id = tab.id,
                    label = tab.label,
                    icon = tab.icon,
                    sections = tab.sections.mapIndexed { index, element -> decodeSection(index, element) },
                )
            },
        )
    }

    private fun decodeSection(index: Int, element: JsonObject): Section =
        decodeOrNull { json.decodeFromJsonElement(Section.serializer(), element) }
            ?: Section.Unsupported(
                id = element.stringOrNull("id") ?: "section-$index",
                originalType = element.stringOrNull("type") ?: "<missing>",
            )

    // SerializationException extends IllegalArgumentException.
    private inline fun <T> decodeOrNull(block: () -> T): T? =
        try {
            block()
        } catch (e: IllegalArgumentException) {
            null
        }

    private fun JsonObject.stringOrNull(key: String): String? =
        runCatching { get(key)?.jsonPrimitive?.contentOrNull }.getOrNull()

    companion object {
        const val SUPPORTED_SCHEMA_VERSION = 1

        val DefaultJson = Json {
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }
    }
}
