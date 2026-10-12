@file:OptIn(ExperimentalLayoutApi::class)

package com.courtneythan.aboutme.feature.profile.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.courtneythan.aboutme.core.designsystem.component.Heading
import com.courtneythan.aboutme.core.designsystem.component.IconBubble
import com.courtneythan.aboutme.core.designsystem.component.InkCard
import com.courtneythan.aboutme.core.designsystem.component.InkChip
import com.courtneythan.aboutme.core.designsystem.component.Inky
import com.courtneythan.aboutme.core.designsystem.component.ListRow
import com.courtneythan.aboutme.core.designsystem.component.PeachButton
import com.courtneythan.aboutme.core.designsystem.icon.InkyIcons
import com.courtneythan.aboutme.core.designsystem.theme.InkyColors
import com.courtneythan.aboutme.core.designsystem.theme.InkyTheme
import com.courtneythan.aboutme.core.model.Action
import com.courtneythan.aboutme.core.model.Section
import com.courtneythan.aboutme.core.model.SkillCard

// ─── About ─────────────────────────────────────────────────────────────────────

@Composable
internal fun AboutSection(section: Section.About, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Heading(section.title)
        InkCard(Modifier.fillMaxWidth(), color = InkyColors.PeachSoft) {
            section.paragraphs.forEach { Text(it, style = InkyTheme.type.body) }
        }
    }
}

// ─── Link list ─────────────────────────────────────────────────────────────────

@Composable
internal fun LinkListSection(section: Section.LinkList, onAction: (Action) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        section.links.forEach { link ->
            ListRow(
                title = link.title,
                subtitle = link.subtitle,
                icon = InkyIcons.named(link.icon),
                onClick = { onAction(link.action) },
            )
        }
    }
}

// ─── Skills ────────────────────────────────────────────────────────────────────

@Composable
internal fun SkillsSection(section: Section.Skills, modifier: Modifier = Modifier) {
    var selected by rememberSaveable(section.id) { mutableStateOf(section.allLabel) }
    val filters = remember(section) { listOf(section.allLabel) + section.cards.map { it.category }.distinct() }
    val visible = if (selected == section.allLabel) section.cards else section.cards.filter { it.category == selected }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            filters.forEach { filter ->
                InkChip(label = filter, selected = filter == selected, onClick = { selected = filter })
            }
        }
        // Two columns. Rows size to their tallest card, so long captions never get cut off.
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            visible.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    row.forEach { card ->
                        SkillTile(
                            card,
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SkillTile(card: SkillCard, modifier: Modifier = Modifier) {
    val featured = card.featured
    InkCard(
        modifier = modifier
            .heightIn(min = 158.dp)
            .semantics(mergeDescendants = true) {},
        color = if (featured) InkyColors.Ink else InkyColors.White,
        contentColor = if (featured) InkyColors.Cream else InkyColors.Ink,
        padding = 14.dp,
    ) {
        IconBubble(
            modifier = Modifier.clearAndSetSemantics { }, // the glyph is decoration
            size = 46.dp,
            color = if (featured) InkyColors.Peach else InkyColors.PeachSoft,
        ) {
            Text(card.glyph, style = InkyTheme.type.subtitle, color = InkyColors.Ink)
        }
        Text(card.title, style = InkyTheme.type.bodyBold)
        Text(card.caption, style = InkyTheme.type.caption, color = if (featured) InkyColors.PeachSoft else InkyColors.Muted)
    }
}

// ─── Contact ───────────────────────────────────────────────────────────────────

@Composable
internal fun ContactSection(section: Section.Contact, onAction: (Action) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = InkyColors.Ink,
        contentColor = InkyColors.Cream,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(InkyColors.Cream),
                    contentAlignment = Alignment.Center,
                ) {
                    Inky(size = 56.dp, modifier = Modifier.padding(top = 6.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Heading(section.title, color = InkyColors.Cream)
                    Text(section.message, style = InkyTheme.type.caption, color = InkyColors.PeachSoft)
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                section.links.forEach { link ->
                    PeachButton(
                        label = link.label,
                        onClick = { onAction(Action.OpenUrl(link.url)) },
                        modifier = Modifier.testTag("contact_${link.kind}"),
                    )
                }
            }
        }
    }
}
