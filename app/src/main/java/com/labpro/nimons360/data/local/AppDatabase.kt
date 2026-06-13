package com.labpro.nimons360.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.labpro.nimons360.data.model.family.PinnedFamilyEntity
import com.labpro.nimons360.data.model.analytics.LocationHistoryEntity
import com.labpro.nimons360.data.model.map.FavoriteLocationEntity

@Database(
    entities = [
        PinnedFamilyEntity::class,
        FavoriteLocationEntity::class,
        LocationHistoryEntity::class,
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun familyDao(): FamilyDao
    abstract fun favoriteLocationDao(): FavoriteLocationDao
    abstract fun locationHistoryDao(): LocationHistoryDao
}
