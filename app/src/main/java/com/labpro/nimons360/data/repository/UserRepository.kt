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

class UserRepository(
    private val tokenManager: TokenManager
) {
    private val _user = MutableStateFlow<UserData?>(null)
    val user: StateFlow<UserData?> = _user.asStateFlow()

    suspend fun getProfile(): NetworkResult<UserData> = safeCall(TAG) {
        val response = RetrofitClient.apiService.getMe()

        if (response.isSuccessful) {
            val currentUser = response.body()!!.data
            _user.value = currentUser

            NetworkResult.Success(currentUser)
        } else {
            NetworkResult.Error("Failed to load profile (HTTP ${response.code()}).")
        }
    }

    suspend fun updateProfile(fullName: String): NetworkResult<UserData> = safeCall(TAG) {
        val response = RetrofitClient.apiService.updateMe(UpdateProfileRequest(fullName))

        if (response.isSuccessful) {
            val updatedUser = response.body()!!.data
            _user.value = updatedUser

            NetworkResult.Success(updatedUser)
        } else {
            NetworkResult.Error("Failed to update profile (HTTP ${response.code()}).")
        }
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}