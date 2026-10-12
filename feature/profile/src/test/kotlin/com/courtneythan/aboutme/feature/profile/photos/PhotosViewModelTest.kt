package com.courtneythan.aboutme.feature.profile.photos

import com.courtneythan.aboutme.core.data.MissingApiKeyException
import com.courtneythan.aboutme.core.data.PhotosRepository
import com.courtneythan.aboutme.core.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class PhotosViewModelTest {

    private val photo = Photo(1, "img", "page", "Linh", "profile", "Boats", "#000000")

    private class FakePhotos(var error: Exception? = null, val result: List<Photo>) : PhotosRepository {
        var calls = 0
        var lastIds: List<Long>? = null
        override suspend fun photos(query: String, count: Int): List<Photo> {
            calls++
            error?.let { throw it }
            return result
        }

        override suspend fun photosById(ids: List<Long>): List<Photo> {
            calls++
            lastIds = ids
            error?.let { throw it }
            return result
        }
    }

    private val search = PhotosRequest.Search("Vietnam", 6)

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads photos for the query`() = runTest {
        val viewModel = PhotosViewModel(FakePhotos(result = listOf(photo)))

        viewModel.load(search)

        assertEquals(PhotosUiState.Content(listOf(photo)), viewModel.uiState.value)
    }

    @Test
    fun `same query twice fetches once`() = runTest {
        val repository = FakePhotos(result = listOf(photo))
        val viewModel = PhotosViewModel(repository)

        viewModel.load(search)
        viewModel.load(search)

        assertEquals(1, repository.calls)
    }

    @Test
    fun `missing key is its own error`() = runTest {
        val viewModel = PhotosViewModel(FakePhotos(error = MissingApiKeyException(), result = emptyList()))

        viewModel.load(search)

        assertEquals(PhotosUiState.Error(missingKey = true), viewModel.uiState.value)
    }

    @Test
    fun `network error then retry recovers`() = runTest {
        val repository = FakePhotos(error = IOException("offline"), result = listOf(photo))
        val viewModel = PhotosViewModel(repository)

        viewModel.load(search)
        assertEquals(PhotosUiState.Error(missingKey = false), viewModel.uiState.value)

        repository.error = null
        viewModel.retry()
        assertEquals(PhotosUiState.Content(listOf(photo)), viewModel.uiState.value)
    }

    @Test
    fun `hand-picked ids use the by-id call`() = runTest {
        val repository = FakePhotos(result = listOf(photo))
        val viewModel = PhotosViewModel(repository)

        viewModel.load(PhotosRequest.ByIds(listOf(20224478L, 14025217L)))

        assertEquals(listOf(20224478L, 14025217L), repository.lastIds)
        assertEquals(PhotosUiState.Content(listOf(photo)), viewModel.uiState.value)
    }

    @Test
    fun `ids win over a query when both are set`() {
        val section = com.courtneythan.aboutme.core.model.Section.Photos(id = "p", photoIds = listOf(1L), query = "Vietnam")
        assertEquals(PhotosRequest.ByIds(listOf(1L)), PhotosRequest.from(section))
    }

    @Test
    fun `json items show immediately without calling the API`() = runTest {
        val repository = FakePhotos(error = MissingApiKeyException(), result = emptyList())
        val viewModel = PhotosViewModel(repository)
        val section = com.courtneythan.aboutme.core.model.Section.Photos(
            id = "p",
            items = listOf(
                com.courtneythan.aboutme.core.model.PhotoItem("img", "page", "Tuan Vy", "profile", "Binh Tay Market"),
            ),
        )

        viewModel.load(PhotosRequest.from(section))

        val content = viewModel.uiState.value as PhotosUiState.Content
        assertEquals("Tuan Vy", content.photos.single().photographer)
        assertEquals(0, repository.calls) // no key needed
    }
}
