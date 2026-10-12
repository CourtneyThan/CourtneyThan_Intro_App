package com.courtneythan.aboutme.feature.profile.places

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.courtneythan.aboutme.core.designsystem.theme.AboutMeTheme
import com.courtneythan.aboutme.core.model.Place
import com.courtneythan.aboutme.core.model.Section
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlacesContentTest {

    @get:Rule
    val compose = createComposeRule()

    private val section = Section.Places("places", "On my travel list", "Live from GraphQL", listOf("JP", "IT", "PE", "IS"))

    private fun show(state: PlacesUiState, onRetry: () -> Unit = {}) =
        compose.setContent { AboutMeTheme { PlacesContent(section, state, onRetry) } }

    @Test
    fun loadingShowsAtMostThreeSkeletons() {
        show(PlacesUiState.Loading)
        compose.onAllNodesWithTag(PlacesTestTags.LOADING).fetchSemanticsNodes().size.let { assertEquals(3, it) }
    }

    @Test
    fun contentShowsEachPlace() {
        show(PlacesUiState.Content(listOf(Place("JP", "Japan", "🇯🇵", "Tokyo", "JPY", "Asia", listOf("Japanese")))))
        compose.onNodeWithTag("place_JP").assertIsDisplayed()
        compose.onNodeWithText("Japan", substring = true).assertIsDisplayed()
    }

    @Test
    fun errorOffersRetry() {
        var retried = false
        show(PlacesUiState.Error, onRetry = { retried = true })
        compose.onNodeWithTag(PlacesTestTags.RETRY).performClick()
        assertEquals(true, retried)
    }
}
