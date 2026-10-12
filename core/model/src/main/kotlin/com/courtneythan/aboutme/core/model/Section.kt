package com.courtneythan.aboutme.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One block of a tab, described by the server (here: profile.json).
 *
 * The JSON field `"type"` picks the subclass. Adding a new section type is a
 * three-step change: add a subclass here, render it in `SectionRenderer`, and the
 * compiler forces every `when` over [Section] to handle it.
 */
@Serializable
sealed class Section {
    abstract val id: String

    /** The big ink header on Home: avatar, name, and stat cards. */
    @Serializable
    @SerialName("hero")
    data class Hero(
        override val id: String,
        val name: String,
        val nickname: String? = null,
        val title: String,
        val stats: List<Stat> = emptyList(),
    ) : Section()

    /** The drippy ink header at the top of the other tabs. */
    @Serializable
    @SerialName("header")
    data class Header(
        override val id: String,
        val title: String,
        val subtitle: String? = null,
        val showBack: Boolean = true,
    ) : Section()

    @Serializable
    @SerialName("about")
    data class About(
        override val id: String,
        val title: String,
        val paragraphs: List<String>,
    ) : Section()

    /** Tappable rows. Each row's [LinkItem.action] tells the app what to do. */
    @Serializable
    @SerialName("link_list")
    data class LinkList(
        override val id: String,
        val links: List<LinkItem>,
    ) : Section()

    /** Filter chips + a grid of skill cards. The chips come from each card's category. */
    @Serializable
    @SerialName("skills")
    data class Skills(
        override val id: String,
        val title: String? = null,
        val allLabel: String = "All",
        val cards: List<SkillCard>,
    ) : Section()

    @Serializable
    @SerialName("timeline")
    data class Timeline(
        override val id: String,
        val entries: List<TimelineEntry>,
    ) : Section()

    @Serializable
    @SerialName("fun_facts")
    data class FunFacts(
        override val id: String,
        val facts: List<FunFact>,
    ) : Section()

    /**
     * Hybrid SDUI: the JSON places this section and names the countries, but the
     * country details are live data from a GraphQL API (see PlacesRepository).
     */
    @Serializable
    @SerialName("places")
    data class Places(
        override val id: String,
        val title: String,
        val subtitle: String? = null,
        val countryCodes: List<String>,
    ) : Section()

    /**
     * A swipeable row of photos, from one of three sources (first match wins):
     *  - [items]: photos spelled out in the JSON (no API key or network call needed),
     *  - [photoIds]: hand-picked Pexels IDs, fetched from the Pexels API in that order,
     *  - [query]: the top [count] Pexels search results.
     */
    @Serializable
    @SerialName("photos")
    data class Photos(
        override val id: String,
        val items: List<PhotoItem> = emptyList(),
        val photoIds: List<Long> = emptyList(),
        val query: String? = null,
        val title: String? = null,
        val count: Int = 6,
    ) : Section() {
        init {
            // Thrown during decoding, so a section with no source becomes Unsupported.
            require(items.isNotEmpty() || photoIds.isNotEmpty() || !query.isNullOrBlank()) {
                "photos needs items, photoIds or a query"
            }
        }
    }

    @Serializable
    @SerialName("contact")
    data class Contact(
        override val id: String,
        val title: String,
        val message: String,
        val links: List<ContactLink>,
    ) : Section()

    /**
     * A section the app couldn't decode: either a type this app version doesn't know
     * (the server is newer than the client) or a known type with bad data.
     * It is filtered out so one broken block never takes down the whole screen.
     */
    @Serializable
    @SerialName("__unsupported")
    data class Unsupported(
        override val id: String,
        val originalType: String,
    ) : Section()
}

/** What happens when the user taps something. Server-defined, like the layout itself. */
@Serializable
sealed class Action {
    @Serializable
    @SerialName("open_tab")
    data class OpenTab(val tabId: String) : Action()

    @Serializable
    @SerialName("open_url")
    data class OpenUrl(val url: String) : Action()
}

/** A photo written directly in the JSON, with the credit its license asks for. */
@Serializable
data class PhotoItem(
    val imageUrl: String,
    val pageUrl: String,
    val photographer: String,
    val photographerUrl: String,
    val alt: String,
)

@Serializable
data class Stat(val value: String, val label: String)

/** [icon] is a name like "star" or "map"; unknown names fall back to a default icon. */
@Serializable
data class LinkItem(
    val title: String,
    val subtitle: String,
    val icon: String = "star",
    val action: Action,
)

@Serializable
data class SkillCard(
    val glyph: String,
    val title: String,
    val caption: String,
    val category: String,
    val featured: Boolean = false,
)

@Serializable
data class TimelineEntry(
    val role: String,
    val company: String,
    val period: String,
    val highlights: List<String> = emptyList(),
    val badge: String? = null,
    val current: Boolean = false,
)

/** A flip card: [front] is the question, [back] is the reveal. */
@Serializable
data class FunFact(val front: String, val back: String)

/** [kind] is free-form ("email", "linkedin", "github", ...) so new kinds never break parsing. */
@Serializable
data class ContactLink(val kind: String, val label: String, val url: String)
