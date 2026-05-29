package com.labpro.nimons360.data.model.user

import com.google.gson.annotations.SerializedName

data class UserData(
    val id: Int,
    val nim: String,
    val email: String,
    @SerializedName("fullName") val fullName: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val profileImageUrl: String? = null,
)