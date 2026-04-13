package com.labpro.nimons360.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.labpro.nimons360.data.repository.FamilyRepository

class FamilyDetailViewModelFactory(
    private val familyId: Int,
    private val repository: FamilyRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FamilyDetailViewModel::class.java))
            return FamilyDetailViewModel(familyId, repository) as T
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}