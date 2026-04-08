package com.labpro.nimons360.data.model.ui_state

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val navigateToMain: Boolean = false,
    val errorMessage: String? = null,
)