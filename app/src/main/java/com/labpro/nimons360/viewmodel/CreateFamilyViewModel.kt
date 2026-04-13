package com.labpro.nimons360.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labpro.nimons360.data.model.NetworkResult
import com.labpro.nimons360.data.model.family_network.CreateFamilyRequest
import com.labpro.nimons360.data.model.ui_state.CreateFamilyUiState
import com.labpro.nimons360.data.repository.FamilyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val FAMILY_ICON_URLS = listOf(
    "https://mad.labpro.hmif.dev/assets/family_icon_1.png",
    "https://mad.labpro.hmif.dev/assets/family_icon_2.png",
    "https://mad.labpro.hmif.dev/assets/family_icon_3.png",
    "https://mad.labpro.hmif.dev/assets/family_icon_4.png",
    "https://mad.labpro.hmif.dev/assets/family_icon_5.png",
    "https://mad.labpro.hmif.dev/assets/family_icon_6.png",
    "https://mad.labpro.hmif.dev/assets/family_icon_7.png",
    "https://mad.labpro.hmif.dev/assets/family_icon_8.png",
)

class CreateFamilyViewModel(private val repository: FamilyRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateFamilyUiState())
    val uiState: StateFlow<CreateFamilyUiState> = _uiState.asStateFlow()

    fun selectIcon(index: Int) {
        _uiState.update { it.copy(selectedIconIndex = index) }
    }

    fun setFamilyName(name: String) {
        _uiState.update { it.copy(familyName = name, error = null) }
    }

    fun create() {
        val name = _uiState.value.familyName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a family name.") }
            return
        }

        val iconUrl = FAMILY_ICON_URLS[_uiState.value.selectedIconIndex]

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.createFamily(CreateFamilyRequest(name, iconUrl))) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isLoading = false, navigateToFamilyId = result.data.data.id)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    /** Called after the Activity has handled the navigation signal. */
    fun onNavigated() {
        _uiState.update { it.copy(navigateToFamilyId = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}