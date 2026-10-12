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
    val ChevronRight = icon("ChevronRight", "M9 5 L16 12 L9 19", strokeWidth = 2.4f)
    val ChevronLeft = icon("ChevronLeft", "M15 5 L8 12 L15 19", strokeWidth = 2.6f)
    val ChevronDown = icon("ChevronDown", "M5 8 L12 15 L19 8", strokeWidth = 2.4f)

    /** Maps the icon names used in profile.json. Unknown names get a star, never a crash. */
    fun named(name: String): ImageVector = when (name.lowercase()) {
        "home" -> Home
        "map" -> Map
        "heart" -> Heart
        else -> Star
    }

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
