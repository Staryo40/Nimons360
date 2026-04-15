package com.labpro.nimons360.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.labpro.nimons360.data.model.family.PinnedFamilyEntity

@Database(
    entities = [PinnedFamilyEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun familyDao(): FamilyDao
}