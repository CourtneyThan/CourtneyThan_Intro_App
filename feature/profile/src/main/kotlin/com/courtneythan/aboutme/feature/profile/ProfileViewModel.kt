package com.courtneythan.aboutme.feature.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.courtneythan.aboutme.core.data.ContentSource
import com.courtneythan.aboutme.core.data.ProfileRepository
import com.courtneythan.aboutme.core.model.Profile
import com.courtneythan.aboutme.core.model.Section
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    /** [profile] only contains sections the app can draw; tabs with nothing left are dropped. */
    @Immutable
    data class Content(
        val profile: Profile,
        val source: ContentSource,
    ) : ProfileUiState

    data object Error : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
) : ViewModel() {

    // Bumping this restarts the pipeline below (used by the Retry button).
    private val retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProfileUiState> = retryTrigger
        .flatMapLatest {
            repository.profile()
                .map<_, ProfileUiState> { snapshot ->
                    val profile = snapshot.profile.withoutUnsupported()
                    if (profile.tabs.isEmpty()) ProfileUiState.Error else ProfileUiState.Content(profile, snapshot.source)
                }
                .onStart { emit(ProfileUiState.Loading) }
                .catch { emit(ProfileUiState.Error) }
        }
        .stateIn(
            scope = viewModelScope,
            // Keep running for 5s after the UI stops collecting, so rotation doesn't restart work.
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState.Loading,
        )

    fun retry() {
        retryTrigger.update { it + 1 }
    }
}

internal fun Profile.withoutUnsupported(): Profile = copy(
    tabs = tabs
        .map { tab -> tab.copy(sections = tab.sections.filterNot { it is Section.Unsupported }) }
        .filter { it.sections.isNotEmpty() },
)
