package com.labpro.nimons360.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labpro.nimons360.data.model.NetworkResult
import com.labpro.nimons360.data.model.ui_state.HomeUiState
import com.labpro.nimons360.data.repository.FamilyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: FamilyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        loadMyFamilies()
        loadDiscoverFamilies()
    }

    private fun loadMyFamilies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMyFamilies = true) }

            when (val result = repository.getMyFamilies()) {
                is NetworkResult.Success -> {
                    val myFamilies = result.data.data

                    _uiState.update {
                        it.copy(
                            isLoadingMyFamilies = false,
                            myFamilies = myFamilies
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMyFamilies = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun loadDiscoverFamilies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDiscover = true) }

            when (val result = repository.getRandomUnjoinedFamilies()) {
                is NetworkResult.Success -> {
                    val discoverFamilies = result.data.data

                    _uiState.update {
                        it.copy(
                            isLoadingDiscover = false,
                            discoverFamilies = discoverFamilies
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingDiscover = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        loadData()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}