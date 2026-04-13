package com.labpro.nimons360.data.model.ui_state

data class CreateFamilyUiState(
    val familyName: String        = "",
    val selectedIconIndex: Int    = 0,        // 0-7
    val isLoading: Boolean        = false,
    /** Non-null = creation succeeded; navigate to this family's detail page. */
    val navigateToFamilyId: Int?  = null,
    val error: String?            = null,
)