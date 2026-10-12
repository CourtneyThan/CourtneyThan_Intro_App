package com.courtneythan.aboutme.feature.profile

import app.cash.turbine.test
import com.courtneythan.aboutme.core.data.ContentSource
import com.courtneythan.aboutme.core.data.ProfileRepository
import com.courtneythan.aboutme.core.data.ProfileSnapshot
import com.courtneythan.aboutme.core.model.Profile
import com.courtneythan.aboutme.core.model.Section
import com.courtneythan.aboutme.core.model.Tab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    /** A repository the test drives by hand: emit, fail, and count subscriptions. */
    private class FakeRepository : ProfileRepository {
        // replay = 1 so an emit never races the ViewModel subscribing.
        val emissions = MutableSharedFlow<ProfileSnapshot>(replay = 1)
        var failNext = false
        var subscriptions = 0

        override fun profile(): Flow<ProfileSnapshot> {
            subscriptions++
            if (failNext) {
                failNext = false
                return kotlinx.coroutines.flow.flow { throw IllegalStateException("boom") }
            }
            return emissions
        }
    }

    private val about = Section.About(id = "a", title = "About", paragraphs = emptyList())
    private val unsupported = Section.Unsupported(id = "x", originalType = "video")

    private fun profile(vararg sections: Section) = Profile(1, null, listOf(Tab("home", "Home", "home", sections.toList())))

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `starts loading then shows content without unsupported sections`() = runTest {
        val repository = FakeRepository()
        val viewModel = ProfileViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ProfileUiState.Loading, awaitItem())
            repository.emissions.emit(ProfileSnapshot(profile(about, unsupported), ContentSource.BUNDLED))
            assertEquals(ProfileUiState.Content(profile(about), ContentSource.BUNDLED), awaitItem())
        }
    }

    @Test
    fun `remote update replaces bundled content`() = runTest {
        val repository = FakeRepository()
        val viewModel = ProfileViewModel(repository)
        val updated = about.copy(title = "Updated")

        viewModel.uiState.test {
            awaitItem() // Loading
            repository.emissions.emit(ProfileSnapshot(profile(about), ContentSource.BUNDLED))
            awaitItem()
            repository.emissions.emit(ProfileSnapshot(profile(updated), ContentSource.REMOTE))
            assertEquals(ProfileUiState.Content(profile(updated), ContentSource.REMOTE), awaitItem())
        }
    }

    @Test
    fun `error then retry resubscribes`() = runTest {
        val repository = FakeRepository().apply { failNext = true }
        val viewModel = ProfileViewModel(repository)

        viewModel.uiState.test {
            // Depending on timing we may or may not observe the initial Loading first.
            val first = awaitItem()
            assertEquals(ProfileUiState.Error, if (first == ProfileUiState.Loading) awaitItem() else first)

            viewModel.retry()
            assertEquals(ProfileUiState.Loading, awaitItem())
            repository.emissions.emit(ProfileSnapshot(profile(about), ContentSource.BUNDLED))
            assertEquals(ProfileUiState.Content(profile(about), ContentSource.BUNDLED), awaitItem())
            assertEquals(2, repository.subscriptions)
        }
    }

    @Test
    fun `tabs left empty after filtering are dropped`() = runTest {
        val repository = FakeRepository()
        val viewModel = ProfileViewModel(repository)
        val two = Profile(
            1,
            null,
            listOf(Tab("home", "Home", "home", listOf(about)), Tab("new", "New", "star", listOf(unsupported))),
        )

        viewModel.uiState.test {
            awaitItem() // Loading
            repository.emissions.emit(ProfileSnapshot(two, ContentSource.BUNDLED))
            val content = awaitItem() as ProfileUiState.Content
            assertEquals(listOf("home"), content.profile.tabs.map { it.id })
        }
    }

    @Test
    fun `nothing drawable becomes Error`() = runTest {
        val repository = FakeRepository()
        val viewModel = ProfileViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // Loading
            repository.emissions.emit(ProfileSnapshot(profile(unsupported), ContentSource.BUNDLED))
            assertEquals(ProfileUiState.Error, awaitItem())
        }
    }
}
