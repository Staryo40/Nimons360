package com.labpro.nimons360.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.labpro.nimons360.data.model.family.PinnedFamilyEntity
import com.labpro.nimons360.data.model.map.FavoriteLocationEntity

@Database(
    entities = [PinnedFamilyEntity::class, FavoriteLocationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun familyDao(): FamilyDao
    abstract fun favoriteLocationDao(): FavoriteLocationDao
}