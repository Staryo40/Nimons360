package com.labpro.nimons360.data.model

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("fullName") val fullName: String,
)