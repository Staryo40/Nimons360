package com.labpro.nimons360.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.labpro.nimons360.data.model.family.PinnedFamilyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {
    @Query("SELECT familyId FROM pinned_families")
    fun observePinnedIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPinned(entity: PinnedFamilyEntity)

    @Query("DELETE FROM pinned_families WHERE familyId = :id")
    suspend fun deletePinned(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM pinned_families WHERE familyId = :id)")
    suspend fun isPinned(id: Int): Boolean
}