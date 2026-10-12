package com.courtneythan.aboutme.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

private val InkyColorScheme = lightColorScheme(
    primary = InkyColors.Peach,
    onPrimary = InkyColors.Ink,
    secondary = InkyColors.Ink,
    onSecondary = InkyColors.Cream,
    background = InkyColors.Cream,
    onBackground = InkyColors.Ink,
    surface = InkyColors.White,
    onSurface = InkyColors.Ink,
)

private val LocalInkyType = staticCompositionLocalOf<InkyType> { error("Wrap content in AboutMeTheme") }

/** Design tokens beyond Material: `InkyTheme.type.display` etc. */
object InkyTheme {
    val type: InkyType
        @Composable get() = LocalInkyType.current
}

/**
 * The ink-and-cream look from Figma. It's intentionally light-only: the design already
 * pairs dark ink headers with a light body, so a separate dark theme would fight it.
 */
@Composable
fun AboutMeTheme(content: @Composable () -> Unit) {
    val (heading, text) = rememberInkyFonts()
    val type = remember(heading, text) { inkyType(heading, text) }
    val typography = remember(type) {
        Typography(
            displaySmall = type.display,
            titleLarge = type.title,
            titleMedium = type.subtitle,
            bodyLarge = type.body,
            bodyMedium = type.body,
            labelLarge = type.label,
            labelMedium = type.caption,
            labelSmall = type.caption,
        )
    }
    CompositionLocalProvider(LocalInkyType provides type) {
        MaterialTheme(colorScheme = InkyColorScheme, typography = typography, content = content)
    }
}
