package com.labpro.nimons360.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labpro.nimons360.data.enums.MainScreenEnum
import com.labpro.nimons360.data.model.NetworkResult
import com.labpro.nimons360.data.model.ui_state.MainUiState
import com.labpro.nimons360.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for the main authenticated flow (Home, Map, Families).
 *
 * Scoped to [MainActivity] so all tabs can read the current user without
 * re-fetching.  Additional state (families, map markers, etc.) will be added
 * per feature in later milestones.
 */
class MainViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(MainScreenEnum.HOME)
    val currentScreen: StateFlow<MainScreenEnum> = _currentScreen.asStateFlow()

    fun setScreen(screen: MainScreenEnum) {
        _currentScreen.value = screen
    }

    val uiState: StateFlow<MainUiState> =
        repository.user
            .map { user ->
                MainUiState(
                    isLoading = user == null,
                    user = user,
                    error = null
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                MainUiState(isLoading = true)
            )

    init {
        viewModelScope.launch {
            repository.getProfile()
        }
    }
}