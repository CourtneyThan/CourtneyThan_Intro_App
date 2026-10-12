package com.courtneythan.aboutme.feature.profile.places

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.courtneythan.aboutme.core.designsystem.component.Heading
import com.courtneythan.aboutme.core.designsystem.component.IconBubble
import com.courtneythan.aboutme.core.designsystem.component.InkCard
import com.courtneythan.aboutme.core.designsystem.component.PeachButton
import com.courtneythan.aboutme.core.designsystem.theme.AboutMeTheme
import com.courtneythan.aboutme.core.designsystem.theme.InkyColors
import com.courtneythan.aboutme.core.designsystem.theme.InkyTheme
import com.courtneythan.aboutme.core.model.Place
import com.courtneythan.aboutme.core.model.Section
import com.courtneythan.aboutme.feature.profile.R

/** Stateful: one ViewModel per places section, fed the country codes from the JSON. */
@Composable
internal fun PlacesSection(
    section: Section.Places,
    modifier: Modifier = Modifier,
    viewModel: PlacesViewModel = hiltViewModel(key = "places-${section.id}"),
) {
    LaunchedEffect(section.countryCodes) { viewModel.load(section.countryCodes) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PlacesContent(section, state, onRetry = viewModel::retry, modifier = modifier)
}

/** Stateless: easy to preview and test without Hilt or a network. */
@Composable
internal fun PlacesContent(
    section: Section.Places,
    state: PlacesUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Heading(section.title)
            section.subtitle?.let { Text(it, style = InkyTheme.type.caption, color = InkyColors.Muted) }
        }
        when (state) {
            PlacesUiState.Loading -> repeat(minOf(section.countryCodes.size, 3)) { PlaceSkeleton() }
            PlacesUiState.Error -> InkCard(Modifier.fillMaxWidth().testTag(PlacesTestTags.ERROR)) {
                Text(stringResource(R.string.places_error), style = InkyTheme.type.body)
                PeachButton(stringResource(R.string.retry), onClick = onRetry, modifier = Modifier.testTag(PlacesTestTags.RETRY))
            }
            is PlacesUiState.Content -> state.places.forEach { PlaceCard(it) }
        }
    }
}

@Composable
private fun PlaceCard(place: Place) {
    InkCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .testTag("place_${place.code}"),
        padding = 14.dp,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Modifier.clearAndSetSemantics { }, size = 52.dp) {
                Text(place.flag, fontSize = 28.sp)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(place.name, style = InkyTheme.type.bodyBold)
                Text(
                    listOfNotNull(place.capital, place.continent).joinToString("  ·  "),
                    style = InkyTheme.type.caption,
                    color = InkyColors.Muted,
                )
                val details = listOfNotNull(
                    place.currency,
                    place.languages.take(2).joinToString(", ").ifEmpty { null },
                )
                if (details.isNotEmpty()) {
                    Text(details.joinToString("  ·  "), style = InkyTheme.type.caption)
                }
            }
        }
    }
}

/** Placeholder card shown while the first response loads. */
@Composable
private fun PlaceSkeleton() {
    InkCard(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { }
            .testTag(PlacesTestTags.LOADING),
        padding = 14.dp,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(size = 52.dp) {}
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonLine(Modifier.fillMaxWidth(0.5f))
                SkeletonLine(Modifier.fillMaxWidth(0.75f))
            }
        }
    }
}

@Composable
private fun SkeletonLine(modifier: Modifier) {
    Row(
        modifier
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(InkyColors.PeachSoft),
    ) {}
}

object PlacesTestTags {
    const val LOADING = "places_loading"
    const val ERROR = "places_error"
    const val RETRY = "places_retry"
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun PlacesPreview() {
    AboutMeTheme {
        PlacesContent(
            section = Section.Places("places", "On my travel list", "Live from GraphQL", listOf("JP", "IT")),
            state = PlacesUiState.Content(
                listOf(
                    Place("JP", "Japan", "🇯🇵", "Tokyo", "JPY", "Asia", listOf("Japanese")),
                    Place("IT", "Italy", "🇮🇹", "Rome", "EUR", "Europe", listOf("Italian")),
                ),
            ),
            onRetry = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
