package com.courtneythan.aboutme.feature.profile.photos

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.courtneythan.aboutme.core.data.MissingApiKeyException
import com.courtneythan.aboutme.core.data.PhotosRepository
import com.courtneythan.aboutme.core.model.Photo
import com.courtneythan.aboutme.core.model.Section
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PhotosUiState {
    data object Loading : PhotosUiState

    @Immutable
    data class Content(val photos: List<Photo>) : PhotosUiState

    /** [missingKey] = no Pexels key configured, so retrying won't help. */
    data class Error(val missingKey: Boolean) : PhotosUiState
}

/** What a photos section asks for: photos written in the JSON, hand-picked IDs, or a search. */
sealed interface PhotosRequest {
    data class Inline(val photos: List<Photo>) : PhotosRequest
    data class ByIds(val ids: List<Long>) : PhotosRequest
    data class Search(val query: String, val count: Int) : PhotosRequest

    companion object {
        /** JSON items win, then hand-picked IDs, then the search query. */
        fun from(section: Section.Photos): PhotosRequest = when {
            section.items.isNotEmpty() -> Inline(
                section.items.mapIndexed { index, item ->
                    Photo(
                        id = index.toLong(),
                        imageUrl = item.imageUrl,
                        pageUrl = item.pageUrl,
                        photographer = item.photographer,
                        photographerUrl = item.photographerUrl,
                        alt = item.alt,
                        averageColor = null,
                    )
                },
            )
            section.photoIds.isNotEmpty() -> ByIds(section.photoIds)
            else -> Search(section.query.orEmpty(), section.count)
        }
    }
}

/** Data for one "photos" section: what to show comes from profile.json, the photos from Pexels. */
@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val repository: PhotosRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhotosUiState>(PhotosUiState.Loading)
    val uiState: StateFlow<PhotosUiState> = _uiState.asStateFlow()

    private var request: PhotosRequest? = null

    /** Called with what the section asks for. The same request again is ignored. */
    fun load(next: PhotosRequest) {
        if (next == request) return
        request = next
        fetch()
    }

    fun retry() = fetch()

    private fun fetch() {
        val current = request ?: return
        if (current is PhotosRequest.Inline) {
            // Everything is already in the JSON: no key, no network.
            _uiState.value = PhotosUiState.Content(current.photos)
            return
        }
        _uiState.value = PhotosUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                val photos = when (current) {
                    is PhotosRequest.Inline -> current.photos
                    is PhotosRequest.ByIds -> repository.photosById(current.ids)
                    is PhotosRequest.Search -> repository.photos(current.query, current.count)
                }
                PhotosUiState.Content(photos)
            } catch (e: CancellationException) {
                throw e
            } catch (e: MissingApiKeyException) {
                PhotosUiState.Error(missingKey = true)
            } catch (e: Exception) {
                PhotosUiState.Error(missingKey = false)
            }
        }
    }
}
