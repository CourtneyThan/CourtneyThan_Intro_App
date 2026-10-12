package com.courtneythan.aboutme.core.model

/**
 * A photo from Pexels, with what the license asks us to show: the photographer's name,
 * and links back to the photographer and the photo on Pexels.
 */
data class Photo(
    val id: Long,
    val imageUrl: String,
    val pageUrl: String,
    val photographer: String,
    val photographerUrl: String,
    /** Description of the photo, used for screen readers. */
    val alt: String,
    /** The photo's average color (e.g. "#7E8C6A"), shown while the image loads. */
    val averageColor: String?,
)
