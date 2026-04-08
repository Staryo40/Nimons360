package com.labpro.nimons360.data.remote

import android.util.Log
import com.labpro.nimons360.core.events.AuthEventBus
import com.labpro.nimons360.core.network.NoAuth
import com.labpro.nimons360.core.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation

/**
 * OkHttp interceptor that:
 * 1. Attaches `Authorization: Bearer <token>` to every outbound request
 *    (skipped for unauthorized requests like /api/login).
 * 2. Detects a 409 (Unauthorized / token expired, per spec) or 401 response,
 *    clears the stored token, and fires [AuthEventBus.emitSessionExpired] so
 *    the UI can redirect to [LoginActivity].
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val invocation = original.tag(Invocation::class.java)
        val noAuth = invocation?.method()?.isAnnotationPresent(NoAuth::class.java) == true
        val token = tokenManager.getToken()

        val finalRequest = if (!noAuth && token != null && tokenManager.isLoggedIn()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        val response = chain.proceed(finalRequest)

        if (!noAuth && (response.code == 401 || response.code == 409)) {
            Log.w(TAG, "Session expired (HTTP ${response.code}). Clearing token.")
            tokenManager.clearToken()
            AuthEventBus.tryEmitSessionExpired()
        }

        return response
    }

    companion object {
        private const val TAG = "AuthInterceptor"
    }
}