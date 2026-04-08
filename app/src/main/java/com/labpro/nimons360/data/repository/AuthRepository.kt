package com.labpro.nimons360.data.repository

import android.util.Log
import com.labpro.nimons360.core.events.AuthEventBus
import com.labpro.nimons360.core.utils.TokenManager
import com.labpro.nimons360.core.utils.safeCall
import com.labpro.nimons360.data.model.NetworkResult
import com.labpro.nimons360.data.model.login.LoginRequest
import com.labpro.nimons360.data.remote.RetrofitClient
import java.time.Instant


class AuthRepository(private val tokenManager: TokenManager) {

    suspend fun login(email: String, password: String): NetworkResult<Unit> = safeCall(TAG) {
        val response = RetrofitClient.apiService.login(LoginRequest(email, password))

        if (response.isSuccessful) {
            val body = response.body() ?: return@safeCall NetworkResult.Error("Empty response")
            val expiresAtMillis = Instant.parse(body.data.expiresAt).toEpochMilli()
            tokenManager.saveToken(
                token = body.data.token,
                expiresAt = expiresAtMillis
            )
            NetworkResult.Success(Unit)
        } else {
            val msg = when (response.code()) {
                400  -> "Invalid email or password."
                401, 409 -> "Unauthorized. Please check your credentials."
                else -> "Login failed (HTTP ${response.code()})."
            }
            NetworkResult.Error(msg, response.code())
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
        AuthEventBus.emitLoggedOut()
        Log.d(TAG, "User logged out.")
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
    fun getToken(): String?   = tokenManager.getToken()

    companion object {
        private const val TAG = "AuthRepository"
    }
}

