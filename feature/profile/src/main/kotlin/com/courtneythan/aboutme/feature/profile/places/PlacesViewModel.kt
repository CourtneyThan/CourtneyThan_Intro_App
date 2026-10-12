package com.courtneythan.aboutme.feature.profile.places

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.courtneythan.aboutme.core.data.PlacesRepository
import com.courtneythan.aboutme.core.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

sealed interface PlacesUiState {
    data object Loading : PlacesUiState

    @Immutable
    data class Content(val places: List<Place>) : PlacesUiState

    data object Error : PlacesUiState
}

/**
 * Data for one "places" section. The section's country codes come from the JSON layout
 * ([load]); the country details come from GraphQL through [PlacesRepository].
 */
@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val repository: PlacesRepository,
) : ViewModel() {

    private val codes = MutableStateFlow<List<String>?>(null)
    private val retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PlacesUiState> = combine(codes.filterNotNull(), retryTrigger) { codes, _ -> codes }
        .flatMapLatest { requested ->
            repository.places(requested)
                .map<_, PlacesUiState> { PlacesUiState.Content(it) }
                .onStart { emit(PlacesUiState.Loading) }
                .catch { emit(PlacesUiState.Error) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlacesUiState.Loading)

    /** Called with the codes from profile.json. Same codes again = no new request. */
    fun load(countryCodes: List<String>) {
        codes.value = countryCodes
    }

    fun retry() {
        retryTrigger.update { it + 1 }
    }
}
