package com.courtneythan.aboutme.feature.profile.sections

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.courtneythan.aboutme.core.designsystem.component.CardShape
import com.courtneythan.aboutme.core.designsystem.component.Decorative
import com.courtneythan.aboutme.core.designsystem.component.rememberReducedMotion
import com.courtneythan.aboutme.core.designsystem.theme.InkyColors
import com.courtneythan.aboutme.core.designsystem.theme.InkyTheme
import com.courtneythan.aboutme.core.model.FunFact
import com.courtneythan.aboutme.core.model.Section
import com.courtneythan.aboutme.feature.profile.R

@Composable
internal fun FunFactsSection(section: Section.FunFacts, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        section.facts.chunked(2).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEachIndexed { colIndex, fact ->
                    FlipCard(
                        fact,
                        Modifier
                            .weight(1f)
                            .testTag("fun_fact_${rowIndex * 2 + colIndex}"),
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** Ink "?" card on the front, peach answer on the back. Flips in 3D; snaps when animations are off. */
@Composable
private fun FlipCard(fact: FunFact, modifier: Modifier = Modifier) {
    var flipped by rememberSaveable(fact.front) { mutableStateOf(false) }
    val reducedMotion = rememberReducedMotion()
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = if (reducedMotion) snap<Float>() else spring<Float>(dampingRatio = 0.7f, stiffness = 300f),
        label = "flip",
    )
    val showBack = rotation > 90f

    Surface(
        onClick = { flipped = !flipped },
        modifier = modifier
            .height(168.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        shape = CardShape,
        color = if (showBack) InkyColors.Peach else InkyColors.Ink,
        contentColor = if (showBack) InkyColors.Ink else InkyColors.Cream,
        border = BorderStroke(2.dp, InkyColors.Ink),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Counter-rotate the back so its text isn't mirrored.
                .graphicsLayer { rotationY = if (showBack) 180f else 0f }
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showBack) {
                Text(stringResource(R.string.flipped), style = InkyTheme.type.caption, textAlign = TextAlign.Center)
                Text(fact.back, style = InkyTheme.type.bodyBold, textAlign = TextAlign.Center)
            } else {
                Decorative("?", style = InkyTheme.type.display, color = InkyColors.Peach)
                Text(fact.front, style = InkyTheme.type.label, textAlign = TextAlign.Center)
            }
        }
    }
}
