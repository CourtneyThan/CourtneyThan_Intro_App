package com.courtneythan.aboutme.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The outline icons from the Figma file (24×24, 2px stroke), built from the same SVG paths.
 * They're drawn in black and tinted by `Icon(tint = ...)`.
 */
object InkyIcons {
    val Home = icon("Home", "M4 11 L12 4 L20 11 V20 H14 V14 H10 V20 H4 Z")
    val Star = icon("Star", "M12 3 L14.6 9 L21 9.6 L16.2 13.8 L17.6 20 L12 16.7 L6.4 20 L7.8 13.8 L3 9.6 L9.4 9 Z")
    val Map = icon(
        "Map",
        "M12 21 C12 21 5 14.5 5 9.5 C5 5.6 8.1 3 12 3 C15.9 3 19 5.6 19 9.5 C19 14.5 12 21 12 21 Z",
        "M9.5 9.5 A2.5 2.5 0 1 0 14.5 9.5 A2.5 2.5 0 1 0 9.5 9.5 Z",
    )
    val Heart = icon(
        "Heart",
        "M12 20 C12 20 3.5 14.5 3.5 8.8 C3.5 6 5.7 4 8.2 4 C10 4 11.3 5 12 6.3 C12.7 5 14 4 15.8 4 C18.3 4 20.5 6 20.5 8.8 C20.5 14.5 12 20 12 20 Z",
    )
    /** A paper lantern (đèn lồng), for the Culture tab. */
    val Lantern = icon(
        "Lantern",
        "M12 2 V5",
        "M9 5 H15",
        "M4.5 11.5 C4.5 8.5 7.8 6.5 12 6.5 C16.2 6.5 19.5 8.5 19.5 11.5 C19.5 14.5 16.2 16.5 12 16.5 C7.8 16.5 4.5 14.5 4.5 11.5 Z",
        "M4.5 11.5 H19.5",
        "M9 18 H15",
        "M12 18 V22",
    )
    /**
     * The Vietnamese flag (cờ đỏ sao vàng), for the Culture tab. Unlike the outline icons
     * it's drawn in its own colors, so it must not be tinted (see [isMulticolor]).
     */
    val VietnamFlag: ImageVector = ImageVector.Builder(
        name = "VietnamFlag",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = addPathNodes("M4.5 5 H19.5 A2.5 2.5 0 0 1 22 7.5 V16.5 A2.5 2.5 0 0 1 19.5 19 H4.5 A2.5 2.5 0 0 1 2 16.5 V7.5 A2.5 2.5 0 0 1 4.5 5 Z"),
            fill = SolidColor(Color(0xFFDA251D)),
        )
        addPath(
            pathData = addPathNodes("M12 7.6 L12.99 10.64 L16.18 10.64 L13.6 12.52 L14.59 15.56 L12 13.68 L9.41 15.56 L10.4 12.52 L7.82 10.64 L11.01 10.64 Z"),
            fill = SolidColor(Color(0xFFFFCD00)),
        )
    }.build()

    val ChevronRight = icon("ChevronRight", "M9 5 L16 12 L9 19", strokeWidth = 2.4f)
    val ChevronLeft = icon("ChevronLeft", "M15 5 L8 12 L15 19", strokeWidth = 2.6f)
    val ChevronDown = icon("ChevronDown", "M5 8 L12 15 L19 8", strokeWidth = 2.4f)

    /** Maps the icon names used in profile.json. Unknown names get a star, never a crash. */
    fun named(name: String): ImageVector = when (name.lowercase()) {
        "home" -> Home
        "map" -> Map
        "heart" -> Heart
        "lantern" -> Lantern
        "flag-vn" -> VietnamFlag
        else -> Star
    }

    /** Icons drawn in their own colors. Callers pass `Color.Unspecified` as the tint for these. */
    fun isMulticolor(name: String): Boolean = name.lowercase() == "flag-vn"

    private fun icon(name: String, vararg paths: String, strokeWidth: Float = 2f): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            paths.forEach { d ->
                addPath(
                    pathData = addPathNodes(d),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = strokeWidth,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()
}
