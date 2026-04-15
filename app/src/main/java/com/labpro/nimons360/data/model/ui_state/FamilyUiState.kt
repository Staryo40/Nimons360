package com.labpro.nimons360.data.model.ui_state

import com.labpro.nimons360.data.enums.FamilyFilter
import com.labpro.nimons360.data.model.family.Family
import kotlin.collections.filter

data class FamilyUiState(
    val allFamilies: List<Family> = emptyList(),
    val myFamilyIds: Set<Int>     = emptySet(),
    val pinnedIds: Set<Int>       = emptySet(),
    val searchQuery: String       = "",
    val filter: FamilyFilter      = FamilyFilter.ALL,
    val isLoading: Boolean        = true,
    val error: String?            = null,
) {
    private val base: List<Family> get() = when (filter) {
        FamilyFilter.ALL         -> allFamilies
        FamilyFilter.MY_FAMILIES -> allFamilies.filter { it.id in myFamilyIds }
    }

    val displayedFamilies: List<Family> get() =
        if (searchQuery.isBlank()) base
        else base.filter { it.name.contains(searchQuery, ignoreCase = true) }
}