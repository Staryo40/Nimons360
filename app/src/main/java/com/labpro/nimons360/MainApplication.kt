package com.labpro.nimons360

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.labpro.nimons360.core.analytics.AppAnalytics
import com.labpro.nimons360.core.utils.TokenManager
import com.labpro.nimons360.data.local.AppDatabase
import com.labpro.nimons360.data.remote.RetrofitClient
import com.labpro.nimons360.data.repository.AuthRepository
import com.labpro.nimons360.data.repository.AnalyticsRepository
import com.labpro.nimons360.data.repository.FamilyRepository
import com.labpro.nimons360.data.repository.LocationRepository
import com.labpro.nimons360.data.repository.UserRepository

class MainApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var tokenManager: TokenManager
        private set

    lateinit var analytics: AppAnalytics
        private set

    val authRepository: AuthRepository by lazy {
        AuthRepository(tokenManager)
    }

    val userRepository: UserRepository by lazy {
        UserRepository(tokenManager)
    }

    val familyRepository: FamilyRepository by lazy {
        FamilyRepository(database.familyDao())
    }

    val locationRepository: LocationRepository by lazy {
        LocationRepository(database.favoriteLocationDao(), applicationContext)
    }

    val analyticsRepository: AnalyticsRepository by lazy {
        AnalyticsRepository(database.locationHistoryDao())
    }

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(applicationContext)
        analytics = AppAnalytics(applicationContext)
        RetrofitClient.initialize(tokenManager)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "nimons_db"
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigrationFrom(1)
            .build()
    }

    private companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE favorite_locations " +
                        "ADD COLUMN description TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE favorite_locations " +
                        "ADD COLUMN photoPaths TEXT NOT NULL DEFAULT '[]'"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS location_history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "latitude REAL NOT NULL, " +
                        "longitude REAL NOT NULL, " +
                        "recordedAt INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_location_history_recordedAt " +
                        "ON location_history(recordedAt)"
                )
            }
        }
    }
}
