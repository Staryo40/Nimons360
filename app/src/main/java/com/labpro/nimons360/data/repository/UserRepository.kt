package com.labpro.nimons360.data.repository

import com.labpro.nimons360.core.utils.safeCall
import com.labpro.nimons360.data.model.NetworkResult
import com.labpro.nimons360.data.model.user.UpdateProfileRequest
import com.labpro.nimons360.data.model.user.UserData
import com.labpro.nimons360.data.remote.RetrofitClient

class UserRepository {

    suspend fun getProfile(): NetworkResult<UserData> = safeCall(TAG) {
        val response = RetrofitClient.apiService.getMe()

        if (response.isSuccessful) {
            NetworkResult.Success(response.body()!!.data)
        } else {
            NetworkResult.Error("Failed to load profile (HTTP ${response.code()}).")
        }
    }

    suspend fun updateProfile(fullName: String): NetworkResult<UserData> = safeCall(TAG) {
        val response = RetrofitClient.apiService.updateMe(UpdateProfileRequest(fullName))

        if (response.isSuccessful) {
            NetworkResult.Success(response.body()!!.data)
        } else {
            NetworkResult.Error("Failed to update profile (HTTP ${response.code()}).")
        }
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}