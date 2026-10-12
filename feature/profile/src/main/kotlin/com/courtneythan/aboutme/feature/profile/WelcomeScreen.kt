package com.courtneythan.aboutme.feature.profile

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.courtneythan.aboutme.core.designsystem.component.Heading
import com.courtneythan.aboutme.core.designsystem.component.InkHeader
import com.courtneythan.aboutme.core.designsystem.component.Inky
import com.courtneythan.aboutme.core.designsystem.component.PagerDots
import com.courtneythan.aboutme.core.designsystem.component.rememberReducedMotion
import com.courtneythan.aboutme.core.designsystem.theme.AboutMeTheme
import com.courtneythan.aboutme.core.designsystem.theme.DripShape
import com.courtneythan.aboutme.core.designsystem.theme.InkyColors
import com.courtneythan.aboutme.core.designsystem.theme.InkyTheme
import com.courtneythan.aboutme.core.model.Welcome

/** The "01 · Welcome" frame: Inky says hi, then "Let's go" opens the tabs. */
@Composable
internal fun WelcomeScreen(welcome: Welcome, onContinue: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(InkyColors.Cream),
    ) {
        InkBlob(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 26.dp, y = 30.dp)
                .size(width = 240.dp, height = 220.dp),
        )

        Column(Modifier.fillMaxSize()) {
            InkHeader(shape = DripShape.Tall) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 28.dp, end = 12.dp, top = 12.dp, bottom = 40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Heading(welcome.greeting, color = InkyColors.Cream, modifier = Modifier.weight(1f))
                    TextButton(onClick = onContinue) {
                        Text(welcome.skip, style = InkyTheme.type.label, color = InkyColors.Cream)
                    }
                }
            }
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                MascotWithBubble(welcome.bubble)
            }
            Column(
                modifier = Modifier.padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Heading(welcome.title, style = InkyTheme.type.display)
                Text(
                    welcome.body,
                    style = InkyTheme.type.body,
                    color = InkyColors.Muted,
                    modifier = Modifier.widthIn(max = 300.dp),
                )
                Spacer(Modifier.height(16.dp))
                PagerDots(count = 3, selected = 0)
            }
            // Room for the round button so it never covers the text.
            Spacer(
                Modifier
                    .navigationBarsPadding()
                    .height(124.dp),
            )
        }

        Surface(
            onClick = onContinue,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 21.dp, bottom = 18.dp)
                .size(96.dp)
                .testTag(ProfileTestTags.WELCOME_CTA),
            shape = CircleShape,
            color = InkyColors.Peach,
            contentColor = InkyColors.Ink,
            border = BorderStroke(3.dp, InkyColors.Cream),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(welcome.cta, style = InkyTheme.type.bodyBold)
            }
        }
    }
}

@Composable
private fun MascotWithBubble(bubble: String) {
    val reducedMotion = rememberReducedMotion()
    val transition = rememberInfiniteTransition(label = "bob")
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "bobOffset",
    )
    Box(Modifier.size(width = 320.dp, height = 330.dp)) {
        // Soft peach shadow under Inky.
        Canvas(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp)
                .size(width = 170.dp, height = 22.dp),
        ) {
            drawOval(InkyColors.Peach.copy(alpha = 0.45f))
        }
        Inky(
            size = 228.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
                .graphicsLayer { translationY = if (reducedMotion) 0f else bob * density },
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = InkyColors.White,
            contentColor = InkyColors.Ink,
            border = BorderStroke(2.dp, InkyColors.Ink),
        ) {
            Text(bubble, style = InkyTheme.type.label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        }
        // Thought-bubble dots leading down to Inky.
        Canvas(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 46.dp, end = 72.dp)
                .size(30.dp),
        ) {
            val stroke = Stroke(width = 2.dp.toPx())
            listOf(Offset(20f, 8f) to 6f, Offset(8f, 24f) to 4f).forEach { (center, r) ->
                val c = Offset(center.x.dp.toPx(), center.y.dp.toPx())
                drawCircle(Color.White, r.dp.toPx(), c)
                drawCircle(InkyColors.Ink, r.dp.toPx(), c, style = stroke)
            }
        }
    }
}

/** The ink splash behind the "Let's go" button (same path as the Figma "Ink blob"). */
@Composable
private fun InkBlob(modifier: Modifier = Modifier) {
    val path = remember {
        PathParser()
            .parsePathString("M60 40 C110 -10 200 0 228 60 C252 112 236 196 160 214 C96 228 20 206 6 150 C-6 100 20 72 60 40 Z")
            .toPath()
    }
    Canvas(modifier) {
        scale(scaleX = size.width / 240f, scaleY = size.height / 220f, pivot = Offset.Zero) {
            drawPath(path, InkyColors.Ink)
        }
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun WelcomePreview() {
    AboutMeTheme { WelcomeScreen(PreviewData.profile.welcome!!, onContinue = {}) }
}
