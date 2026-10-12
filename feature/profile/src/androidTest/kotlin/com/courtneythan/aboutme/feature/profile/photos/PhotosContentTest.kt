package com.courtneythan.aboutme.feature.profile.photos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.courtneythan.aboutme.core.designsystem.theme.AboutMeTheme
import com.courtneythan.aboutme.core.model.Photo
import com.courtneythan.aboutme.core.model.Section
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotosContentTest {

    @get:Rule
    val compose = createComposeRule()

    private val section = Section.Photos(id = "p", query = "Vietnam")
    private val photo = Photo(7, "https://example.com/7.jpg", "https://pexels/7", "Linh Tran", "https://pexels/@linh", "Boats", null)

    private fun show(state: PhotosUiState, onOpenUrl: (String) -> Unit = {}, onRetry: () -> Unit = {}) =
        compose.setContent { AboutMeTheme { PhotosContent(section, state, onRetry, onOpenUrl) } }

    @Test
    fun contentCreditsThePhotographerAndPexels() {
        val opened = mutableListOf<String>()
        show(PhotosUiState.Content(listOf(photo)), onOpenUrl = { opened += it })

        compose.onNodeWithText("Photo by Linh Tran").assertIsDisplayed().performClick()
        compose.onNodeWithTag(PhotosTestTags.CREDIT).assertIsDisplayed().performClick()

        assertEquals(listOf("https://pexels/@linh", "https://www.pexels.com"), opened)
    }

    @Test
    fun missingKeyExplainsTheSetupWithoutRetry() {
        show(PhotosUiState.Error(missingKey = true))
        compose.onNodeWithText("PEXELS_API_KEY", substring = true).assertIsDisplayed()
        compose.onNodeWithTag(PhotosTestTags.RETRY).assertDoesNotExist()
    }

    @Test
    fun networkErrorOffersRetry() {
        var retried = false
        show(PhotosUiState.Error(missingKey = false), onRetry = { retried = true })
        compose.onNodeWithTag(PhotosTestTags.RETRY).performClick()
        assertEquals(true, retried)
    }
}
