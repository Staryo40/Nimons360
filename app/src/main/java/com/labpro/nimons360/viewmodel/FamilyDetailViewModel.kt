package com.labpro.nimons360.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.labpro.nimons360.data.model.NetworkResult
import com.labpro.nimons360.data.model.family_network.JoinFamilyRequest
import com.labpro.nimons360.data.model.family_network.LeaveFamilyRequest
import com.labpro.nimons360.data.model.ui_state.FamilyDetailUiState
import com.labpro.nimons360.data.repository.FamilyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class FamilyDetailViewModel(
    private val familyId: Int,
    private val repository: FamilyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyDetailUiState())
    val uiState: StateFlow<FamilyDetailUiState> = _uiState.asStateFlow()

    init { loadFamily() }

    // ── Data loading ──────────────────────────────────────────────────────────

    fun loadFamily() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = repository.getFamilyDetail(familyId)) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isLoading = false, family = r.data.data)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = r.message)
                }
            }
        }
    }

    /**
     * Join the family using the 6-character [code] from [JoinFamilyDialog].
     * On success, reload the family detail so [isMember] flips to true and the
     * member list becomes fully visible.
     */
    fun joinFamily(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionError = null) }
            when (val r = repository.joinFamily(JoinFamilyRequest(familyId, code))) {
                is NetworkResult.Success -> {
                    // Reload so the UI reflects full membership (code, members).
                    loadFamily()
                    _uiState.update {
                        it.copy(isActionLoading = false, snackbarMessage = "Joined family!")
                    }
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isActionLoading = false, actionError = r.message)
                }
            }
        }
    }

    /**
     * Leave the family. On success, signal the Fragment to dismiss/navigate back.
     */
    fun leaveFamily() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionError = null) }
            val r = repository.leaveFamily(LeaveFamilyRequest(familyId))
            when (r) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isActionLoading = false, navigateBack = true)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isActionLoading = false, actionError = r.message)
                }
            }
        }
    }

    fun onNavigatedBack()  { _uiState.update { it.copy(navigateBack = false) } }
    fun clearActionError() { _uiState.update { it.copy(actionError = null) } }
    fun clearSnackbar()    { _uiState.update { it.copy(snackbarMessage = null) } }
}