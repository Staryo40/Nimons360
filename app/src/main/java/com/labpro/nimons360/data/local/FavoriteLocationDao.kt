package com.labpro.nimons360.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.labpro.nimons360.data.model.map.FavoriteLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {
    @Query("SELECT * FROM favorite_locations ORDER BY title COLLATE NOCASE ASC, id ASC")
    fun observeFavoriteLocations(): Flow<List<FavoriteLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(entity: FavoriteLocationEntity)

    @Query("DELETE FROM favorite_locations WHERE latitude = :latitude AND longitude = :longitude")
    suspend fun deleteLocation(latitude: Double, longitude: Double)

    @Query("DELETE FROM favorite_locations WHERE id = :id")
    suspend fun deleteLocation(id: Int)
}
