package com.labpro.nimons360.data.model.family

data class FamilyWithMembers(
    val id: Int,
    val name: String,
    val iconUrl: String,
    val familyCode: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val members: List<FamilyMember> = emptyList(),
)