package com.courtneythan.aboutme.feature.profile.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.courtneythan.aboutme.core.designsystem.component.Badge
import com.courtneythan.aboutme.core.designsystem.component.Decorative
import com.courtneythan.aboutme.core.designsystem.component.InkCard
import com.courtneythan.aboutme.core.designsystem.icon.InkyIcons
import com.courtneythan.aboutme.core.designsystem.theme.InkyColors
import com.courtneythan.aboutme.core.designsystem.theme.InkyTheme
import com.courtneythan.aboutme.core.model.Section
import com.courtneythan.aboutme.core.model.TimelineEntry
import com.courtneythan.aboutme.feature.profile.R

@Composable
internal fun TimelineSection(section: Section.Timeline, modifier: Modifier = Modifier) {
    var mode by rememberSaveable(section.id) { mutableIntStateOf(0) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SegmentedControl(
            options = listOf(stringResource(R.string.timeline_tab), stringResource(R.string.highlights_tab)),
            selected = mode,
            onSelect = { mode = it },
        )
        if (mode == 0) {
            Column {
                section.entries.forEachIndexed { index, entry ->
                    TimelineRow(entry, isFirst = index == 0, isLast = index == section.entries.lastIndex)
                }
            }
        } else {
            HighlightsList(section.entries)
        }
    }
}

@Composable
private fun SegmentedControl(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .border(2.dp, InkyColors.Ink, CircleShape)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            Text(
                text = label,
                style = InkyTheme.type.label,
                color = if (isSelected) InkyColors.Cream else InkyColors.Ink,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) InkyColors.Ink else InkyColors.Cream)
                    .selectable(selected = isSelected, onClick = { onSelect(index) }, role = Role.Tab)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun TimelineRow(entry: TimelineEntry, isFirst: Boolean, isLast: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        Rail(
            isFirst = isFirst,
            isLast = isLast,
            highlighted = entry.current,
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
        )
        TimelineCard(
            entry,
            Modifier
                .weight(1f)
                .padding(start = 8.dp, bottom = if (isLast) 0.dp else 10.dp),
        )
    }
}

/** The line and dot to the left of each card. Segments join up across rows into one rail. */
@Composable
private fun Rail(isFirst: Boolean, isLast: Boolean, highlighted: Boolean, modifier: Modifier = Modifier) {
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val x = size.width / 2
            val dotY = 22.dp.toPx()
            val line = 3.dp.toPx()
            val radius = 9.dp.toPx()
            if (!isFirst) drawLine(InkyColors.Ink, Offset(x, 0f), Offset(x, dotY), strokeWidth = line)
            if (!isLast) drawLine(InkyColors.Ink, Offset(x, dotY), Offset(x, size.height), strokeWidth = line)
            drawCircle(if (highlighted) InkyColors.Peach else InkyColors.Cream, radius, Offset(x, dotY))
            drawCircle(InkyColors.Ink, radius, Offset(x, dotY), style = Stroke(width = line))
        }
    }
}

@Composable
private fun TimelineCard(entry: TimelineEntry, modifier: Modifier = Modifier) {
    // The current role is always open; past roles open on tap.
    var expanded by rememberSaveable(entry.role, entry.period) { mutableStateOf(entry.current) }
    val canToggle = !entry.current && entry.highlights.isNotEmpty()
    val stateText = stringResource(if (expanded) R.string.state_expanded else R.string.state_collapsed)
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")

    InkCard(
        modifier = modifier.semantics { if (canToggle) stateDescription = stateText },
        color = if (entry.current) InkyColors.PeachSoft else InkyColors.White,
        onClick = if (canToggle) ({ expanded = !expanded }) else null,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.role, style = InkyTheme.type.bodyBold)
                    if (entry.current) Badge(stringResource(R.string.now), color = InkyColors.Ink, contentColor = InkyColors.Cream)
                }
                Text("${entry.company}  ·  ${entry.period}", style = InkyTheme.type.caption, color = InkyColors.Muted)
                entry.badge?.let { Badge("★  $it", modifier = Modifier.padding(top = 2.dp)) }
            }
            if (canToggle) {
                Icon(
                    InkyIcons.ChevronDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(chevronRotation),
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                entry.highlights.forEach { highlight ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Decorative("•", style = InkyTheme.type.caption)
                        Text(highlight, style = InkyTheme.type.caption)
                    }
                }
            }
        }
    }
}

/** The "Highlights" view: every highlight as its own card, tagged with where it happened. */
@Composable
private fun HighlightsList(entries: List<TimelineEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.forEach { entry ->
            entry.highlights.forEach { highlight ->
                InkCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                    color = if (entry.current) InkyColors.PeachSoft else InkyColors.White,
                    padding = 14.dp,
                ) {
                    Text(highlight, style = InkyTheme.type.bodyBold)
                    Text("${entry.role}  ·  ${entry.company}", style = InkyTheme.type.caption, color = InkyColors.Muted)
                }
            }
        }
    }
}
