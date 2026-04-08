package com.labpro.nimons360.data.model.login

import com.labpro.nimons360.data.model.UserData

data class LoginData(
    val token: String,
    val expiresAt: String,
    val user: UserData,
)