package com.courtneythan.aboutme.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.courtneythan.aboutme.core.data.ContentSource
import com.courtneythan.aboutme.core.designsystem.theme.InkyColors
import com.courtneythan.aboutme.core.designsystem.theme.InkyTheme
import com.courtneythan.aboutme.core.model.Action
import com.courtneythan.aboutme.core.model.Tab
import com.courtneythan.aboutme.feature.profile.sections.SectionRenderer

/** One tab: its server-defined sections, top to bottom. */
@Composable
internal fun TabScreen(
    tab: Tab,
    source: ContentSource,
    onAction: (Action) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        // key(tab.id): each tab starts scrolled to the top.
        key(tab.id) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 600.dp) // stays readable on tablets and foldables
                    .testTag(ProfileTestTags.LIST),
                // Leave room for the floating bottom nav.
                contentPadding = PaddingValues(bottom = navBar + 112.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(tab.sections, key = { it.id }, contentType = { it::class }) { section ->
                    SectionRenderer(section = section, onAction = onAction, onBack = onBack)
                }
                item(key = "footer") {
                    Text(
                        text = stringResource(R.string.profile_footer, source.label()),
                        style = InkyTheme.type.caption,
                        color = InkyColors.Muted,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentSource.label(): String = stringResource(
    when (this) {
        ContentSource.BUNDLED -> R.string.source_bundled
        ContentSource.CACHE -> R.string.source_cache
        ContentSource.REMOTE -> R.string.source_remote
    },
)
