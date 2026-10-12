package com.courtneythan.aboutme.core.model

import kotlinx.serialization.Serializable

/**
 * The whole app, as described by profile.json:
 * an optional [welcome] screen, then one [Tab] per bottom-nav item.
 */
data class Profile(
    val schemaVersion: Int,
    val welcome: Welcome?,
    val tabs: List<Tab>,
)

/** One bottom-nav destination. [icon] is a name like "home", "star", "map" or "heart". */
data class Tab(
    val id: String,
    val label: String,
    val icon: String,
    val sections: List<Section>,
)

/** The first screen people see. Optional: if it's missing or broken, the app opens on the first tab. */
@Serializable
data class Welcome(
    val greeting: String,
    val bubble: String,
    val title: String,
    val body: String,
    val cta: String,
    val skip: String = "Skip",
)
