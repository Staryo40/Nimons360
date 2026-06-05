package com.labpro.nimons360.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labpro.nimons360.data.enums.FamilyFilter
import com.labpro.nimons360.data.model.NetworkResult
import com.labpro.nimons360.data.model.ui_state.FamilyUiState
import com.labpro.nimons360.data.repository.FamilyRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FamilyViewModel(private val repository: FamilyRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyUiState())
    val uiState: StateFlow<FamilyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePinnedIds().collect { ids ->
                _uiState.update { it.copy(pinnedIds = ids.toSet()) }
            }
        }

        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val allJob = async { repository.getAllFamilies() }
            val myJob  = async { repository.getMyFamilies() }

            val allResult = allJob.await()
            val myResult  = myJob.await()

            _uiState.update { state ->
                val myFamilies = when (myResult) {
                    is NetworkResult.Success -> myResult.data.data
                    is NetworkResult.Error   -> emptyList()
                }

                val myFamilyIds = myFamilies.map { it.id }.toSet()
                val myFamiliesMap = myFamilies.associateBy { it.id }

                val allFamilies = when (allResult) {
                    is NetworkResult.Success -> {
                        allResult.data.data.map { family ->
                            val joinedFamily = myFamiliesMap[family.id]
                            if (joinedFamily != null) {
                                family.copy(members = joinedFamily.members)
                            } else {
                                family
                            }
                        }
                    }
                    is NetworkResult.Error -> state.allFamilies
                }

                val error = (allResult as? NetworkResult.Error)?.message
                    ?: (myResult as? NetworkResult.Error)?.message

                state.copy(
                    isLoading   = false,
                    allFamilies = allFamilies,
                    myFamilyIds = myFamilyIds,
                    error       = error,
                )
            }
        }
    }

    fun setSearch(query: String) =
        _uiState.update { it.copy(searchQuery = query) }

    fun setFilter(filter: FamilyFilter) =
        _uiState.update { it.copy(filter = filter) }

    fun togglePin(familyId: Int) {
        viewModelScope.launch {
            repository.togglePin(familyId)
        }
    }
}