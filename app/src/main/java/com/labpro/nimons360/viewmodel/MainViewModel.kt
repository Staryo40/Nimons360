package com.labpro.nimons360.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labpro.nimons360.data.model.NetworkResult
import com.labpro.nimons360.data.model.ui_state.MainUiState
import com.labpro.nimons360.data.repository.AuthRepository
import com.labpro.nimons360.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = MainUiState(isLoading = true)

            when (val result = repository.getProfile()) {
                is NetworkResult.Success -> {
                    _uiState.value = MainUiState(user = result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = MainUiState(error = result.message)
                }
            }
        }
    }
}