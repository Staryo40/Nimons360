package com.labpro.nimons360.data.model.user

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("fullName") val fullName: String,
)