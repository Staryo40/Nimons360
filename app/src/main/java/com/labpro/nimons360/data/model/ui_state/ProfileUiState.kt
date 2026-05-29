package com.labpro.nimons360.data.model.ui_state

data class ProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val email: String = "",
    val error: String? = null,
    val isLoggedOut: Boolean = false,
    val profileImageUrl: String? = null
)