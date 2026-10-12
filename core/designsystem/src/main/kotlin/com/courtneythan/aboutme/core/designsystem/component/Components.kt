package com.courtneythan.aboutme.core.designsystem.component

import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.courtneythan.aboutme.core.designsystem.R
import com.courtneythan.aboutme.core.designsystem.icon.InkyIcons
import com.courtneythan.aboutme.core.designsystem.theme.DripShape
import com.courtneythan.aboutme.core.designsystem.theme.InkyColors
import com.courtneythan.aboutme.core.designsystem.theme.InkyTheme

val CardShape = RoundedCornerShape(22.dp)
private val InkBorder = BorderStroke(2.dp, InkyColors.Ink)

/**
 * Ink background that melts into drips at the bottom.
 * Content sits above the drips; [overlap] lets the last bit of content hang over them.
 */
@Composable
fun InkHeader(
    modifier: Modifier = Modifier,
    shape: DripShape = DripShape.Short,
    overlap: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(InkyColors.Ink, shape)
            .padding(bottom = (shape.dripHeight - overlap).coerceAtLeast(0.dp)),
        content = content,
    )
}

/** Inky, the ink-blob mascot. Decorative, so screen readers skip it. */
@Composable
fun Inky(modifier: Modifier = Modifier, size: Dp = 120.dp) {
    Image(
        painter = painterResource(R.drawable.inky),
        contentDescription = null,
        modifier = modifier.size(size),
    )
}

/** Rounded card with the chunky 2dp ink outline. Pass [onClick] to make the whole card tappable. */
@Composable
fun InkCard(
    modifier: Modifier = Modifier,
    color: Color = InkyColors.White,
    contentColor: Color = InkyColors.Ink,
    padding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val body: @Composable () -> Unit = {
        Column(Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
    if (onClick == null) {
        Surface(modifier = modifier, shape = CardShape, color = color, contentColor = contentColor, border = InkBorder, content = body)
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = CardShape,
            color = color,
            contentColor = contentColor,
            border = InkBorder,
            content = body,
        )
    }
}

/** Filter chip: ink when selected, white with an ink outline when not. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InkChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = if (selected) InkyColors.Ink else InkyColors.White,
        contentColor = if (selected) InkyColors.White else InkyColors.Ink,
        border = InkBorder,
    ) {
        Text(label, style = InkyTheme.type.label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    }
}

/** A round peach bubble holding an icon or a short glyph. */
@Composable
fun IconBubble(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    color: Color = InkyColors.PeachSoft,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** Tappable row from the "List Row" component: icon bubble, title, subtitle, chevron. */
@Composable
fun ListRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = InkyColors.White,
        contentColor = InkyColors.Ink,
        border = InkBorder,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IconBubble { Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = InkyTheme.type.bodyBold)
                Text(subtitle, style = InkyTheme.type.caption, color = InkyColors.Muted)
            }
            Icon(InkyIcons.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

/** The peach pill button. */
@Composable
fun PeachButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = InkyColors.Peach,
        contentColor = InkyColors.Ink,
        border = InkBorder,
    ) {
        Text(label, style = InkyTheme.type.bodyBold, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp))
    }
}

/** Small rounded label, e.g. "NOW" or an award. */
@Composable
fun Badge(text: String, modifier: Modifier = Modifier, color: Color = InkyColors.Peach, contentColor: Color = InkyColors.Ink) {
    Text(
        text = text,
        style = InkyTheme.type.caption,
        color = contentColor,
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

@Immutable
data class NavItem(val label: String, val icon: ImageVector)

/** Floating ink pill with one round slot per tab; the selected slot turns peach. */
@Composable
fun BottomNav(
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(InkyColors.Ink)
            .padding(8.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (selected) InkyColors.Peach else Color.Transparent)
                    .selectable(selected = selected, onClick = { onSelect(index) }, role = Role.Tab)
                    .semantics { contentDescription = item.label },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (selected) InkyColors.Ink else InkyColors.Cream,
                )
            }
        }
    }
}

/** Section heading, marked as a heading so TalkBack users can jump between sections. */
@Composable
fun Heading(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = InkyTheme.type.title,
    color: Color = InkyColors.Ink,
    textAlign: TextAlign? = null,
) {
    Text(text, style = style, color = color, textAlign = textAlign, modifier = modifier.semantics { heading() })
}

/** Pager dots from the welcome screen (purely decorative). */
@Composable
fun PagerDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { i ->
            if (i == selected) {
                Box(
                    Modifier
                        .size(width = 28.dp, height = 10.dp)
                        .clip(CircleShape)
                        .background(InkyColors.Peach),
                )
            } else {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(InkyColors.Ink),
                )
            }
        }
    }
}

/** Text used only as decoration (e.g. a bullet). Hidden from screen readers. */
@Composable
fun Decorative(text: String, modifier: Modifier = Modifier, style: TextStyle = InkyTheme.type.body, color: Color = Color.Unspecified) {
    Text(text, style = style, color = color, modifier = modifier.clearAndSetSemantics { })
}

@Composable
fun VerticalGap(height: Dp) = Spacer(Modifier.size(width = 0.dp, height = height))

/** True when the user turned off animations in system settings. */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
