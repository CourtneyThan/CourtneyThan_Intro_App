package com.courtneythan.aboutme.feature.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.courtneythan.aboutme.core.data.ContentSource
import com.courtneythan.aboutme.core.designsystem.component.BottomNav
import com.courtneythan.aboutme.core.designsystem.component.Heading
import com.courtneythan.aboutme.core.designsystem.component.Inky
import com.courtneythan.aboutme.core.designsystem.component.NavItem
import com.courtneythan.aboutme.core.designsystem.component.PeachButton
import com.courtneythan.aboutme.core.designsystem.icon.InkyIcons
import com.courtneythan.aboutme.core.designsystem.theme.AboutMeTheme
import com.courtneythan.aboutme.core.designsystem.theme.InkyColors
import com.courtneythan.aboutme.core.model.Action
import com.courtneythan.aboutme.core.model.Profile

/** Stateful entry point: wires the ViewModel. */
@Composable
fun ProfileRoute(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    ProfileScreen(
        state = state,
        onRetry = viewModel::retry,
        onOpenUrl = { url -> runCatching { uriHandler.openUri(url) } },
    )
}

/** Stateless screen: easy to preview and to test without Hilt. */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onRetry: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(InkyColors.Cream),
    ) {
        when (state) {
            ProfileUiState.Loading -> CircularProgressIndicator(
                color = InkyColors.Ink,
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag(ProfileTestTags.LOADING),
            )

            ProfileUiState.Error -> ErrorState(onRetry, Modifier.align(Alignment.Center))

            is ProfileUiState.Content -> AboutMeApp(state.profile, state.source, onOpenUrl)
        }
    }
}

/**
 * Welcome screen first (if the content has one), then the tabs.
 * Navigation state is plain saveable state: there are only two levels, so a nav graph would be overkill.
 */
@Composable
private fun AboutMeApp(profile: Profile, source: ContentSource, onOpenUrl: (String) -> Unit) {
    var showWelcome by rememberSaveable { mutableStateOf(profile.welcome != null) }
    var selectedTabId by rememberSaveable { mutableStateOf(profile.tabs.first().id) }
    // If new content removes the selected tab, fall back to the first one.
    val selectedIndex = profile.tabs.indexOfFirst { it.id == selectedTabId }.coerceAtLeast(0)
    val homeId = profile.tabs.first().id

    val welcome = profile.welcome
    if (showWelcome && welcome != null) {
        WelcomeScreen(welcome = welcome, onContinue = { showWelcome = false })
        return
    }

    BackHandler(enabled = selectedIndex != 0) { selectedTabId = homeId }

    val onAction: (Action) -> Unit = { action ->
        when (action) {
            is Action.OpenTab -> if (profile.tabs.any { it.id == action.tabId }) selectedTabId = action.tabId
            is Action.OpenUrl -> onOpenUrl(action.url)
        }
    }

    Box(Modifier.fillMaxSize()) {
        TabScreen(
            tab = profile.tabs[selectedIndex],
            source = source,
            onAction = onAction,
            onBack = { selectedTabId = homeId },
        )
        BottomNav(
            items = profile.tabs.map { NavItem(it.label, InkyIcons.named(it.icon)) },
            selectedIndex = selectedIndex,
            onSelect = { selectedTabId = profile.tabs[it].id },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .testTag(ProfileTestTags.NAV),
        )
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Inky(size = 120.dp)
        Heading(stringResource(R.string.error_title))
        PeachButton(stringResource(R.string.retry), onClick = onRetry, modifier = Modifier.testTag(ProfileTestTags.RETRY))
    }
}

object ProfileTestTags {
    const val LOADING = "profile_loading"
    const val LIST = "profile_list"
    const val RETRY = "profile_retry"
    const val NAV = "bottom_nav"
    const val WELCOME_CTA = "welcome_cta"
}

@Preview(showBackground = true, heightDp = 852, widthDp = 393)
@Composable
private fun AppPreview() {
    AboutMeTheme {
        ProfileScreen(
            state = ProfileUiState.Content(PreviewData.profile.copy(welcome = null), ContentSource.BUNDLED),
            onRetry = {},
            onOpenUrl = {},
        )
    }
}
