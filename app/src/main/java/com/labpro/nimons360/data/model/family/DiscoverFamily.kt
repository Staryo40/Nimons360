package com.labpro.nimons360.data.model.family

data class DiscoverFamily(
    val id: Int,
    val name: String,
    val iconUrl: String,
    val createdAt: String? = null,
    val members: List<FamilyMember> = emptyList(), // Members names censored from server
)