package com.labpro.nimons360.data.repository

import coil.util.CoilUtils.result
import com.labpro.nimons360.core.utils.TokenManager
import com.labpro.nimons360.core.utils.safeCall
import com.labpro.nimons360.data.model.NetworkResult
import com.labpro.nimons360.data.model.user.UpdateProfileRequest
import com.labpro.nimons360.data.model.user.UserData
import com.labpro.nimons360.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MultipartBody

class UserRepository(
    private val tokenManager: TokenManager
) {
    private val _user = MutableStateFlow<UserData?>(null)
    val user: StateFlow<UserData?> = _user.asStateFlow()

    suspend fun getProfile(): NetworkResult<UserData> = safeCall(TAG) {
        val response = RetrofitClient.apiService.getMe()

        if (response.isSuccessful) {
            val currentUser = response.body()!!.data
            val resolvedUser = if (!currentUser.profileImageUrl.isNullOrBlank()) {
                currentUser.copy(
                    profileImageUrl = "${currentUser.profileImageUrl}?t=${System.currentTimeMillis()}"
                )
            } else {
                currentUser
            }
            _user.value = resolvedUser

            NetworkResult.Success(resolvedUser)
        } else {
            NetworkResult.Error("Failed to load profile (HTTP ${response.code()}).")
        }
    }

    suspend fun updateProfile(fullName: String): NetworkResult<UserData> = safeCall(TAG) {
        val response = RetrofitClient.apiService.updateMe(UpdateProfileRequest(fullName))

        if (response.isSuccessful) {
            val updatedUser = response.body()!!.data
            val resolvedUser = if (!updatedUser.profileImageUrl.isNullOrBlank()) {
                updatedUser.copy(
                    profileImageUrl = "${updatedUser.profileImageUrl}?t=${System.currentTimeMillis()}"
                )
            } else {
                updatedUser
            }
            _user.value = resolvedUser

            NetworkResult.Success(resolvedUser)
        } else {
            NetworkResult.Error("Failed to update profile (HTTP ${response.code()}).")
        }
    }

    suspend fun uploadProfilePhoto(photoPart: MultipartBody.Part): NetworkResult<UserData> = safeCall(TAG) {
        val response = RetrofitClient.apiService.uploadProfilePhoto(photoPart)

        if (response.isSuccessful) {
            val updatedUser = response.body()!!.data
            val resolvedUser = if (!updatedUser.profileImageUrl.isNullOrBlank()) {
                updatedUser.copy(
                    profileImageUrl = "${updatedUser.profileImageUrl}?t=${System.currentTimeMillis()}"
                )
            } else {
                updatedUser
            }
            _user.value = resolvedUser

            NetworkResult.Success(resolvedUser)
        } else {
            NetworkResult.Error("Failed to upload profile photo (HTTP ${response.code()}).")
        }
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}