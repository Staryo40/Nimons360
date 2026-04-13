package com.labpro.nimons360.data.model.ui_state

import com.labpro.nimons360.data.model.family.FamilyDetail

data class FamilyDetailUiState(
    val isLoading: Boolean       = true,
    val family: FamilyDetail?    = null,
    val error: String?           = null,
    // True while a join/leave network call is in-flight
    val isActionLoading: Boolean = false,
    val actionError: String?     = null,
    // One-shot signals consumed by the Fragment
    val navigateBack: Boolean    = false,
    val snackbarMessage: String? = null,
)