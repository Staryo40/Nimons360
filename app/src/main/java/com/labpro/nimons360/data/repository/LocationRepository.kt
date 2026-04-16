package com.labpro.nimons360.data.repository

import com.labpro.nimons360.data.local.FavoriteLocationDao
import com.labpro.nimons360.data.model.map.FavoriteLocationEntity
import kotlinx.coroutines.flow.Flow

class LocationRepository(private val dao: FavoriteLocationDao) {
    fun observeFavoriteLocations(): Flow<List<FavoriteLocationEntity>> {
        return dao.observeFavoriteLocations()
    }

    suspend fun addFavoriteLocation(latitude: Double, longitude: Double, title: String) {
        dao.insertLocation(
            FavoriteLocationEntity(
                latitude = latitude,
                longitude = longitude,
                title = title
            )
        )
    }

    suspend fun removeFavoriteLocation(latitude: Double, longitude: Double) {
        dao.deleteLocation(latitude, longitude)
    }
}