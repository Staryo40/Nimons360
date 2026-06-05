package com.labpro.nimons360.data.remote

import com.labpro.nimons360.core.network.NoAuth
import com.labpro.nimons360.data.model.family_network.CreateFamilyRequest
import com.labpro.nimons360.data.model.family_network.DiscoverFamiliesResponse
import com.labpro.nimons360.data.model.family_network.FamilyDetailResponse
import com.labpro.nimons360.data.model.family_network.FamilyListResponse
import com.labpro.nimons360.data.model.family_network.JoinFamilyRequest
import com.labpro.nimons360.data.model.family_network.JoinFamilyResponse
import com.labpro.nimons360.data.model.family_network.LeaveFamilyRequest
import com.labpro.nimons360.data.model.family_network.LeaveFamilyResponse
import com.labpro.nimons360.data.model.family_network.MyFamiliesResponse
import com.labpro.nimons360.data.model.user.UpdateProfileRequest
import com.labpro.nimons360.data.model.user.UserResponse
import com.labpro.nimons360.data.model.login.LoginRequest
import com.labpro.nimons360.data.model.login.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody

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

    /** POST /api/me/photo — uploads a new profile photo. */
    @Multipart
    @POST("/api/me/photo")
    suspend fun uploadProfilePhoto(
        @Part photo: MultipartBody.Part,
    ): Response<UserResponse>

    // === Families ===
    /** GET /api/families — all families (id, name, iconUrl only). */
    @GET("/api/families")
    suspend fun getAllFamilies(): Response<FamilyListResponse>

    /** GET /api/me/families — families the current user has joined, with members. */
    @GET("/api/me/families")
    suspend fun getMyFamilies(): Response<MyFamiliesResponse>

    /** GET /api/families/discover — 5 random families the user hasn't joined. */
    @GET("/api/families/discover")
    suspend fun discoverFamilies(): Response<DiscoverFamiliesResponse>

    /** GET /api/families/:id */
    @GET("/api/families/{familyId}")
    suspend fun getFamilyDetail(@Path("familyId") familyId: Int): Response<FamilyDetailResponse>

    /** POST /api/families */
    @POST("/api/families")
    suspend fun createFamily(@Body request: CreateFamilyRequest): Response<FamilyDetailResponse>

    /** POST /api/families/join */
    @POST("/api/families/join")
    suspend fun joinFamily(@Body request: JoinFamilyRequest): Response<JoinFamilyResponse>

    /** POST /api/families/leave */
    @POST("/api/families/leave")
    suspend fun leaveFamily(@Body request: LeaveFamilyRequest): Response<LeaveFamilyResponse>

    // === Notifications ===

    @POST("/api/notifications/subscribe")
    suspend fun subscribeDeviceToken(
        @Body request: com.labpro.nimons360.data.model.notification.SubscribeTokenRequest
    ): Response<com.labpro.nimons360.data.model.notification.SubscribeTokenResponse>

    @POST("/api/notifications/unsubscribe")
    suspend fun unsubscribeDeviceToken(): Response<com.labpro.nimons360.data.model.notification.UnsubscribeTokenResponse>

    @POST("/api/notifications/send")
    suspend fun sendBroadcastNotification(
        @Body request: com.labpro.nimons360.data.model.notification.BroadcastNotificationRequest
    ): Response<com.labpro.nimons360.data.model.notification.BroadcastNotificationResponse>

    @POST("/api/notifications/greeting")
    suspend fun sendGreetingToMember(
        @Body request: com.labpro.nimons360.data.model.notification.SendGreetingRequest
    ): Response<com.labpro.nimons360.data.model.notification.SendGreetingResponse>
}