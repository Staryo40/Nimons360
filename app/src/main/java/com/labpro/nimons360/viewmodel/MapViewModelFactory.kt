package com.labpro.nimons360.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.labpro.nimons360.data.model.user.UserData
import com.labpro.nimons360.data.repository.LocationRepository

class MapViewModelFactory(
    private val user: UserData,
    private val token: () -> String?,
    private val locationRepository: LocationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(user, token, locationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}