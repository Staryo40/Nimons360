package com.labpro.nimons360.data.repository

import com.labpro.nimons360.core.utils.safeCall
import com.labpro.nimons360.data.local.FamilyDao
import com.labpro.nimons360.data.model.NetworkResult
import com.labpro.nimons360.data.model.family.PinnedFamilyEntity
import com.labpro.nimons360.data.model.family_network.CreateFamilyRequest
import com.labpro.nimons360.data.model.family_network.DiscoverFamiliesResponse
import com.labpro.nimons360.data.model.family_network.FamilyDetailResponse
import com.labpro.nimons360.data.model.family_network.FamilyListResponse
import com.labpro.nimons360.data.model.family_network.JoinFamilyRequest
import com.labpro.nimons360.data.model.family_network.JoinFamilyResponse
import com.labpro.nimons360.data.model.family_network.LeaveFamilyRequest
import com.labpro.nimons360.data.model.family_network.LeaveFamilyResponse
import com.labpro.nimons360.data.model.family_network.MyFamiliesResponse
import com.labpro.nimons360.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow

class FamilyRepository(
    private val dao: FamilyDao
) {

    suspend fun getAllFamilies(): NetworkResult<FamilyListResponse> = safeCall(TAG) {
        val response = RetrofitClient.apiService.getAllFamilies()

        val body = response.body()
        if (response.isSuccessful && body != null) {
            NetworkResult.Success(body)
        } else {
            NetworkResult.Error("Failed to fetch all families.")
        }
    }

    suspend fun getMyFamilies(): NetworkResult<MyFamiliesResponse> = safeCall(TAG) {
        val response = RetrofitClient.apiService.getMyFamilies()

        val body = response.body()
        if (response.isSuccessful && body != null) {
            NetworkResult.Success(body)
        } else {
            NetworkResult.Error("Failed to fetch user families.")
        }
    }

    suspend fun getRandomUnjoinedFamilies(): NetworkResult<DiscoverFamiliesResponse> = safeCall(TAG) {
        val response = RetrofitClient.apiService.discoverFamilies()

        val body = response.body()
        if (response.isSuccessful && body != null) {
            NetworkResult.Success(body)
        } else {
            NetworkResult.Error("Failed to fetch random unjoined families.")
        }
    }

    suspend fun getFamilyDetail(familyId: Int): NetworkResult<FamilyDetailResponse> = safeCall(TAG) {
        val response = RetrofitClient.apiService.getFamilyDetail(familyId)

        val body = response.body()
        if (response.isSuccessful && body != null) {
            NetworkResult.Success(body)
        } else {
            NetworkResult.Error("Failed to fetch family $familyId detail.")
        }
    }

    suspend fun createFamily(request: CreateFamilyRequest): NetworkResult<FamilyDetailResponse> = safeCall(TAG) {
        val response = RetrofitClient.apiService.createFamily(request)

        val body = response.body()
        if (response.isSuccessful && body != null) {
            NetworkResult.Success(body)
        } else {
            NetworkResult.Error("Failed to create ${request.name} family.")
        }
    }

    suspend fun joinFamily(request: JoinFamilyRequest): NetworkResult<JoinFamilyResponse> = safeCall(TAG) {
        val response = RetrofitClient.apiService.joinFamily(request)

        val body = response.body()
        if (response.isSuccessful && body != null) {
            NetworkResult.Success(body)
        } else {
            NetworkResult.Error("Failed to join family ${request.familyId} with code ${request.familyCode}.")
        }
    }

    suspend fun leaveFamily(request: LeaveFamilyRequest): NetworkResult<LeaveFamilyResponse> = safeCall(TAG) {
        val response = RetrofitClient.apiService.leaveFamily(request)

        val body = response.body()
        if (response.isSuccessful && body != null) {
            NetworkResult.Success(body)
        } else {
            NetworkResult.Error("Failed to leave family ${request.familyId}.")
        }
    }

    fun observePinnedIds(): Flow<List<Int>> {
        return dao.observePinnedIds()
    }

    suspend fun togglePin(familyId: Int) {
        val isPinned = dao.isPinned(familyId)
        if (isPinned) {
            dao.deletePinned(familyId)
        } else {
            dao.insertPinned(PinnedFamilyEntity(familyId))
        }
    }

    companion object {
        private const val TAG = "FamilyRepository"
    }
}