package com.courtneythan.aboutme.core.model

/**
 * A country on the travel list. The app's own model, so the UI never depends on
 * Apollo's generated classes (they change whenever the GraphQL query changes).
 */
data class Place(
    val code: String,
    val name: String,
    val flag: String,
    val capital: String?,
    val currency: String?,
    val continent: String,
    val languages: List<String>,
)
