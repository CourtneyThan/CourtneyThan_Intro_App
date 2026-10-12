package com.courtneythan.aboutme.core.designsystem.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** The type ramp from the Figma file's text styles. */
@Immutable
data class InkyType(
    val display: TextStyle,
    val title: TextStyle,
    val subtitle: TextStyle,
    val body: TextStyle,
    val bodyBold: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
)

internal fun inkyType(heading: FontFamily, text: FontFamily) = InkyType(
    display = TextStyle(fontFamily = heading, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 1.15.em),
    title = TextStyle(fontFamily = heading, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 1.2.em),
    subtitle = TextStyle(fontFamily = heading, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 1.25.em),
    body = TextStyle(fontFamily = text, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 1.45.em),
    bodyBold = TextStyle(fontFamily = text, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 1.4.em),
    label = TextStyle(fontFamily = text, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 1.3.em),
    caption = TextStyle(fontFamily = text, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 1.35.em),
)

/**
 * Fredoka (headings) and Nunito (text), loaded from assets/fonts.
 *
 * The font files aren't checked in (run scripts/download_fonts.sh once). If they're
 * missing, the app falls back to the system font instead of crashing, so a fresh
 * clone always builds and runs.
 */
@Composable
internal fun rememberInkyFonts(): Pair<FontFamily, FontFamily> {
    val context = LocalContext.current
    return remember(context) {
        val heading = context.variableFontFamily("fonts/fredoka.ttf", FontWeight.Medium, FontWeight.SemiBold)
        val text = context.variableFontFamily("fonts/nunito.ttf", FontWeight.Normal, FontWeight.SemiBold, FontWeight.Bold)
        (heading ?: FontFamily.Default) to (text ?: FontFamily.Default)
    }
}

@OptIn(ExperimentalTextApi::class)
private fun Context.variableFontFamily(path: String, vararg weights: FontWeight): FontFamily? {
    val name = path.substringAfterLast('/')
    val present = runCatching { assets.list("fonts")?.contains(name) == true }.getOrDefault(false)
    if (!present) return null
    // One variable font file serves every weight; the weight axis is set per entry.
    return FontFamily(weights.map { Font(path = path, assetManager = assets, weight = it) })
}
