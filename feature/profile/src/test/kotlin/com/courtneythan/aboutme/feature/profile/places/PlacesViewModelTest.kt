package com.courtneythan.aboutme.feature.profile.places

import app.cash.turbine.test
import com.courtneythan.aboutme.core.data.PlacesRepository
import com.courtneythan.aboutme.core.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PlacesViewModelTest {

    private val japan = Place("JP", "Japan", "🇯🇵", "Tokyo", "JPY", "Asia", listOf("Japanese"))

    private class FakePlaces(var fail: Boolean = false) : PlacesRepository {
        val requests = mutableListOf<List<String>>()
        override fun places(codes: List<String>): Flow<List<Place>> {
            requests += codes
            return if (fail) flow { throw IOException("offline") } else flowOf(listOf(PLACE))
        }

        companion object {
            val PLACE = Place("JP", "Japan", "🇯🇵", "Tokyo", "JPY", "Asia", listOf("Japanese"))
        }
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `waits for codes, then loads them`() = runTest {
        val repository = FakePlaces()
        val viewModel = PlacesViewModel(repository)

        viewModel.uiState.test {
            assertEquals(PlacesUiState.Loading, awaitItem())
            viewModel.load(listOf("JP"))
            assertEquals(PlacesUiState.Content(listOf(japan)), expectMostRecentItem())
            assertEquals(listOf(listOf("JP")), repository.requests)
        }
    }

    @Test
    fun `same codes again do not refetch`() = runTest {
        val repository = FakePlaces()
        val viewModel = PlacesViewModel(repository)

        viewModel.uiState.test {
            viewModel.load(listOf("JP"))
            viewModel.load(listOf("JP"))
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, repository.requests.size)
    }

    @Test
    fun `error then retry recovers`() = runTest {
        val repository = FakePlaces(fail = true)
        val viewModel = PlacesViewModel(repository)

        viewModel.uiState.test {
            viewModel.load(listOf("JP"))
            assertEquals(PlacesUiState.Error, expectMostRecentItem())

            repository.fail = false
            viewModel.retry()
            assertEquals(PlacesUiState.Content(listOf(japan)), expectMostRecentItem())
            assertEquals(2, repository.requests.size)
        }
    }
}
