package com.labpro.nimons360.data.remote

import com.labpro.nimons360.core.network.NoAuth
import com.labpro.nimons360.data.model.user.UpdateProfileRequest
import com.labpro.nimons360.data.model.user.UserResponse
import com.labpro.nimons360.data.model.login.LoginRequest
import com.labpro.nimons360.data.model.login.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

/**
 * Retrofit service interface for the Nimons360 REST API.
 * Base URL is injected from BuildConfig.BASE_URL (set in build.gradle.kts).
 *
 * Authentication: All endpoints except login require
 *   Authorization: Bearer <token>
 * This header is attached automatically by [AuthInterceptor].
 */
interface ApiService {

    // === Auth ====
    @NoAuth
    @POST("/api/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): Response<LoginResponse>

    // === Profile ===

    /** GET /api/me — returns the logged-in user's profile. */
    @GET("/api/me")
    suspend fun getMe(): Response<UserResponse>

    /** PATCH /api/me — updates the logged-in user's display name. */
    @PATCH("/api/me")
    suspend fun updateMe(
        @Body request: UpdateProfileRequest,
    ): Response<UserResponse>
}