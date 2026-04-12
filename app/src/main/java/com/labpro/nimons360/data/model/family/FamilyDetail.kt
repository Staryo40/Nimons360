package com.labpro.nimons360.data.model.family

data class FamilyDetail(
    val id: Int,
    val name: String,
    val iconUrl: String,
    val isMember: Boolean,
    val familyCode: String? = null,   // only present when isMember = true
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val members: List<FamilyMember> = emptyList(),
)