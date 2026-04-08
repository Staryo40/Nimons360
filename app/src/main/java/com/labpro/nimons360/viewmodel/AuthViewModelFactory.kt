package com.labpro.nimons360.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.labpro.nimons360.data.repository.AuthRepository

/**
 * Required because [AuthViewModel] has a constructor parameter.
 * Usage:
 *   val vm: AuthViewModel by viewModels {
 *       AuthViewModelFactory((application as MainApplication).authRepository)
 *   }
 */
class AuthViewModelFactory(
    private val repository: AuthRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}