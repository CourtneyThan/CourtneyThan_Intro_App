package com.courtneythan.aboutme.feature.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.courtneythan.aboutme.core.data.ContentSource
import com.courtneythan.aboutme.core.designsystem.theme.AboutMeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun setContent(
        state: ProfileUiState = ProfileUiState.Content(PreviewData.profile, ContentSource.BUNDLED),
        onRetry: () -> Unit = {},
        onOpenUrl: (String) -> Unit = {},
    ) {
        compose.setContent {
            AboutMeTheme { ProfileScreen(state = state, onRetry = onRetry, onOpenUrl = onOpenUrl) }
        }
    }

    private fun skipWelcome() = compose.onNodeWithTag(ProfileTestTags.WELCOME_CTA).performClick()

    @Test
    fun showsLoadingIndicator() {
        setContent(ProfileUiState.Loading)
        compose.onNodeWithTag(ProfileTestTags.LOADING).assertIsDisplayed()
    }

    @Test
    fun welcomeComesFirstThenHome() {
        setContent()
        compose.onNodeWithText("MEET COURTNEY").assertIsDisplayed()
        skipWelcome()
        compose.onNodeWithText("Courtney Than").assertIsDisplayed()
        compose.onNodeWithText("aka Thien  ·  Android Engineer").assertIsDisplayed()
    }

    @Test
    fun linkRowOpensItsTab() {
        setContent()
        skipWelcome()
        compose.onNodeWithTag(ProfileTestTags.LIST).performScrollToNode(androidx.compose.ui.test.hasText("My toolbox"))
        compose.onNodeWithText("My toolbox").performClick()
        compose.onNodeWithText("Jetpack Compose").assertIsDisplayed()
        compose.onNodeWithContentDescription("Toolbox").assertIsSelected()
    }

    @Test
    fun skillFilterHidesOtherCategories() {
        setContent()
        skipWelcome()
        compose.onNodeWithContentDescription("Toolbox").performClick()
        compose.onNodeWithText("Testing", useUnmergedTree = true).performClick()
        compose.onNodeWithText("JUnit & Compose UI", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("Kotlin").assertDoesNotExist()
    }

    @Test
    fun backFromTabGoesHome() {
        setContent()
        skipWelcome()
        compose.onNodeWithContentDescription("Journey").performClick()
        compose.onNodeWithContentDescription("Back to home").performClick()
        compose.onNodeWithText("Courtney Than").assertIsDisplayed()
    }

    @Test
    fun flipCardRevealsTheAnswer() {
        setContent()
        skipWelcome()
        compose.onNodeWithContentDescription("Fun facts").performClick()
        compose.onNodeWithTag("fun_fact_0").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Employee of the Month!", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun contactButtonOpensLink() {
        var opened: String? = null
        setContent(onOpenUrl = { opened = it })
        skipWelcome()
        compose.onNodeWithContentDescription("Fun facts").performClick()
        compose.onNodeWithTag(ProfileTestTags.LIST).performScrollToNode(hasTestTag("contact_email"))
        compose.onNodeWithTag("contact_email").performClick()
        assertEquals("mailto:hello@example.com", opened)
    }

    @Test
    fun retryButtonCallsBack() {
        var retried = false
        setContent(ProfileUiState.Error, onRetry = { retried = true })
        compose.onNodeWithTag(ProfileTestTags.RETRY).performClick()
        assertEquals(true, retried)
    }
}
