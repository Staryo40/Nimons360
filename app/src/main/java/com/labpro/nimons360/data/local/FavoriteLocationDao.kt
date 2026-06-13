package com.labpro.nimons360.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.labpro.nimons360.data.model.map.FavoriteLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {
    @Query("SELECT * FROM favorite_locations ORDER BY title COLLATE NOCASE ASC, id ASC")
    fun observeFavoriteLocations(): Flow<List<FavoriteLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(entity: FavoriteLocationEntity): Long

    @Update
    suspend fun updateLocation(entity: FavoriteLocationEntity)

    @Query("DELETE FROM favorite_locations WHERE id = :id")
    suspend fun deleteLocation(id: Int)

    @Query("SELECT * FROM favorite_locations WHERE id = :id LIMIT 1")
    suspend fun getLocation(id: Int): FavoriteLocationEntity?
}
