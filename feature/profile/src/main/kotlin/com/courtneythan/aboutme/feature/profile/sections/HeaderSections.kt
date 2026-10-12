package com.courtneythan.aboutme.feature.profile.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.courtneythan.aboutme.core.designsystem.component.Heading
import com.courtneythan.aboutme.core.designsystem.component.InkHeader
import com.courtneythan.aboutme.core.designsystem.component.Inky
import com.courtneythan.aboutme.core.designsystem.icon.InkyIcons
import com.courtneythan.aboutme.core.designsystem.theme.DripShape
import com.courtneythan.aboutme.core.designsystem.theme.InkyColors
import com.courtneythan.aboutme.core.designsystem.theme.InkyTheme
import com.courtneythan.aboutme.core.model.Section
import com.courtneythan.aboutme.core.model.Stat
import com.courtneythan.aboutme.feature.profile.R

// ─── Hero (Home header) ────────────────────────────────────────────────────────

@Composable
internal fun HeroSection(section: Section.Hero, modifier: Modifier = Modifier) {
    // The stat cards hang over the drips, like in the Figma design.
    InkHeader(modifier, shape = DripShape.Hero, overlap = if (section.stats.isEmpty()) 0.dp else 36.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .clip(CircleShape)
                    .background(InkyColors.Cream)
                    .border(4.dp, InkyColors.Peach, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Inky(size = 102.dp, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(14.dp))
            Heading(section.name, style = InkyTheme.type.display, color = InkyColors.Cream, textAlign = TextAlign.Center)
            Text(
                text = listOfNotNull(section.nickname?.let { stringResource(R.string.hero_aka, it) }, section.title)
                    .joinToString("  ·  "),
                style = InkyTheme.type.body,
                color = InkyColors.PeachSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (section.stats.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    section.stats.forEach {
                        StatCard(
                            it,
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(stat: Stat, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .heightIn(min = 78.dp)
            .clip(shape)
            .background(InkyColors.White)
            .border(2.dp, InkyColors.Ink, shape)
            .padding(horizontal = 6.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) {}, // read "30+ PRs to core app" as one item
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stat.value, style = InkyTheme.type.title, color = InkyColors.Ink)
        Text(stat.label, style = InkyTheme.type.caption, color = InkyColors.Muted, textAlign = TextAlign.Center)
    }
}

// ─── Header (other tabs) ───────────────────────────────────────────────────────

@Composable
internal fun HeaderSection(section: Section.Header, onBack: () -> Unit, modifier: Modifier = Modifier) {
    InkHeader(modifier, shape = DripShape.Short) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 24.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (section.showBack) {
                IconButton(onClick = onBack) {
                    Icon(InkyIcons.ChevronLeft, contentDescription = stringResource(R.string.back), tint = InkyColors.Cream)
                }
            } else {
                Spacer(Modifier.width(16.dp))
            }
            Heading(section.title, style = InkyTheme.type.display, color = InkyColors.Cream)
            section.subtitle?.let {
                Spacer(Modifier.width(14.dp))
                Text(it, style = InkyTheme.type.label, color = InkyColors.PeachSoft, modifier = Modifier.weight(1f))
            }
        }
    }
}
