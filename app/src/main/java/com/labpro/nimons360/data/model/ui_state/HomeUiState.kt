package com.labpro.nimons360.data.model.ui_state

import com.labpro.nimons360.data.model.family.DiscoverFamily
import com.labpro.nimons360.data.model.family.Family
import com.labpro.nimons360.data.model.family.FamilyWithMembers

data class HomeUiState(
    val isLoadingMyFamilies: Boolean = false,
    val myFamilies: List<FamilyWithMembers> = emptyList(),

    val isLoadingDiscover: Boolean = false,
    val discoverFamilies: List<DiscoverFamily> = emptyList(),

    val errorMessage: String? = null
)