package com.labpro.nimons360.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.labpro.nimons360.data.model.analytics.LocationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationHistoryDao {
    @Insert
    suspend fun insert(item: LocationHistoryEntity)

    @Query("SELECT * FROM location_history ORDER BY recordedAt DESC LIMIT 1")
    suspend fun latest(): LocationHistoryEntity?

    @Query(
        "SELECT * FROM location_history " +
            "WHERE recordedAt >= :fromInclusive AND recordedAt < :toExclusive " +
            "ORDER BY recordedAt ASC"
    )
    suspend fun between(fromInclusive: Long, toExclusive: Long): List<LocationHistoryEntity>

    @Query("SELECT * FROM location_history ORDER BY recordedAt ASC")
    suspend fun all(): List<LocationHistoryEntity>

    @Query("SELECT * FROM location_history ORDER BY recordedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<LocationHistoryEntity>>
}
