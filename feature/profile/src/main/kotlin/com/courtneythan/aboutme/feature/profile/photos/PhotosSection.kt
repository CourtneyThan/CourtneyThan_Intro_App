package com.courtneythan.aboutme.feature.profile.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.courtneythan.aboutme.core.designsystem.component.Heading
import com.courtneythan.aboutme.core.designsystem.component.InkCard
import com.courtneythan.aboutme.core.designsystem.component.PeachButton
import com.courtneythan.aboutme.core.designsystem.theme.InkyColors
import com.courtneythan.aboutme.core.designsystem.theme.InkyTheme
import com.courtneythan.aboutme.core.model.Action
import com.courtneythan.aboutme.core.model.Photo
import com.courtneythan.aboutme.core.model.Section
import com.courtneythan.aboutme.feature.profile.R

private const val PEXELS_URL = "https://www.pexels.com"
private val PhotoShape = RoundedCornerShape(22.dp)

/** Stateful: one ViewModel per photos section, fed the query from the JSON. */
@Composable
internal fun PhotosSection(
    section: Section.Photos,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhotosViewModel = hiltViewModel(key = "photos-${section.id}"),
) {
    LaunchedEffect(section) { viewModel.load(PhotosRequest.from(section)) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PhotosContent(section, state, onRetry = viewModel::retry, onOpenUrl = { onAction(Action.OpenUrl(it)) }, modifier = modifier)
}

/** Stateless: easy to preview and test without Hilt or a network. */
@Composable
internal fun PhotosContent(
    section: Section.Photos,
    state: PhotosUiState,
    onRetry: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        section.title?.let { Heading(it) }
        when (state) {
            PhotosUiState.Loading -> LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false,
            ) {
                items(2) {
                    Box(
                        Modifier
                            .size(width = 260.dp, height = 172.dp)
                            .clip(PhotoShape)
                            .background(InkyColors.PeachSoft)
                            .testTag(PhotosTestTags.LOADING),
                    )
                }
            }

            is PhotosUiState.Error -> InkCard(Modifier.fillMaxWidth().testTag(PhotosTestTags.ERROR)) {
                Text(
                    stringResource(if (state.missingKey) R.string.photos_missing_key else R.string.photos_error),
                    style = InkyTheme.type.body,
                )
                if (!state.missingKey) {
                    PeachButton(stringResource(R.string.retry), onClick = onRetry, modifier = Modifier.testTag(PhotosTestTags.RETRY))
                }
            }

            is PhotosUiState.Content -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.photos, key = { it.id }) { photo -> PhotoCard(photo, onOpenUrl) }
                }
                // Pexels asks apps to show a visible link back to Pexels.
                Text(
                    text = stringResource(R.string.photos_credit),
                    style = InkyTheme.type.caption,
                    color = InkyColors.Muted,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable(role = Role.Button) { onOpenUrl(PEXELS_URL) }
                        .padding(vertical = 4.dp)
                        .testTag(PhotosTestTags.CREDIT),
                )
            }
        }
    }
}

@Composable
private fun PhotoCard(photo: Photo, onOpenUrl: (String) -> Unit) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .testTag("photo_${photo.id}"),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AsyncImage(
            model = photo.imageUrl,
            contentDescription = photo.alt,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 260.dp, height = 172.dp)
                .clip(PhotoShape)
                .background(photo.averageColor.toColorOr(InkyColors.PeachSoft))
                .border(2.dp, InkyColors.Ink, PhotoShape)
                .clickable(role = Role.Button) { onOpenUrl(photo.pageUrl) },
        )
        // Credit the photographer, linking to their Pexels profile.
        Text(
            text = stringResource(R.string.photo_by, photo.photographer),
            style = InkyTheme.type.caption,
            color = InkyColors.Muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .clickable(role = Role.Button) { onOpenUrl(photo.photographerUrl) }
                .padding(horizontal = 4.dp),
        )
    }
}

/** Parses "#RRGGBB" from Pexels; anything else falls back to [fallback]. */
private fun String?.toColorOr(fallback: Color): Color =
    this?.removePrefix("#")?.takeIf { it.length == 6 }?.toLongOrNull(16)?.let { Color(0xFF000000 or it) } ?: fallback

object PhotosTestTags {
    const val LOADING = "photos_loading"
    const val ERROR = "photos_error"
    const val RETRY = "photos_retry"
    const val CREDIT = "photos_credit"
}
