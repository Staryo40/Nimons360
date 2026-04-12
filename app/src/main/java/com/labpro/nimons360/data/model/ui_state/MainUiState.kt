package com.labpro.nimons360.data.model.ui_state

import com.labpro.nimons360.data.model.user.UserData

data class MainUiState(
    val isLoading: Boolean = false,
    val user: UserData? = null,
    val error: String? = null
)