package com.labpro.nimons360.data.repository

import com.labpro.nimons360.data.local.LocationHistoryDao
import com.labpro.nimons360.data.model.analytics.LocationHistoryEntity
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AnalyticsRepository(
    private val dao: LocationHistoryDao,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    private val recordMutex = Mutex()

    fun observeRecent(limit: Int = 5): Flow<List<LocationHistoryEntity>> = dao.observeRecent(limit)

    suspend fun recordLocation(
        latitude: Double,
        longitude: Double,
        recordedAt: Long = System.currentTimeMillis(),
    ) = recordMutex.withLock {
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return
        val latest = dao.latest()
        if (latest != null) {
            val elapsed = recordedAt - latest.recordedAt
            val distance = distanceMeters(
                latest.latitude,
                latest.longitude,
                latitude,
                longitude,
            )
            if (elapsed < MIN_SAMPLE_INTERVAL_MS || distance < MIN_SAMPLE_DISTANCE_METERS) return
            if (distance > MAX_REASONABLE_JUMP_METERS && elapsed < MAX_JUMP_INTERVAL_MS) return
        }
        dao.insert(
            LocationHistoryEntity(
                latitude = latitude,
                longitude = longitude,
                recordedAt = recordedAt,
            )
        )
    }

    suspend fun summary(now: Long = System.currentTimeMillis()): DistanceAnalytics {
        val all = dao.all()
        val month = YearMonth.from(Instant.ofEpochMilli(now).atZone(zoneId))
        val monthStart = month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val monthEnd = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val currentMonth = all.filter { it.recordedAt in monthStart until monthEnd }
        val daily = distancesByDay(currentMonth, month)
        val activeDays = daily.count { it.distanceKm > 0.0 }
        val currentMonthDistance = daily.sumOf(DailyDistance::distanceKm)
        val monthTotals = all
            .groupBy { YearMonth.from(Instant.ofEpochMilli(it.recordedAt).atZone(zoneId)) }
            .values
            .map(::totalDistanceKm)
            .filter { it > 0.0 }

        return DistanceAnalytics(
            month = month,
            monthlyDistanceAverageKm = monthTotals.averageOrZero(),
            currentMonthDistanceKm = currentMonthDistance,
            totalDistanceKm = totalDistanceKm(all),
            dailyDistanceAverageKm = if (activeDays == 0) 0.0 else currentMonthDistance / activeDays,
            activeDays = activeDays,
            dailyDistances = daily,
            recentLocations = all.asReversed().take(5),
        )
    }

    suspend fun allHistory(): List<LocationHistoryEntity> = dao.all()

    private fun distancesByDay(
        items: List<LocationHistoryEntity>,
        month: YearMonth,
    ): List<DailyDistance> {
        val grouped = items.groupBy {
            Instant.ofEpochMilli(it.recordedAt).atZone(zoneId).toLocalDate()
        }
        return (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            DailyDistance(date, totalDistanceKm(grouped[date].orEmpty()))
        }
    }

    private fun totalDistanceKm(items: List<LocationHistoryEntity>): Double {
        return items.zipWithNext().sumOf { (start, end) ->
            distanceMeters(
                start.latitude,
                start.longitude,
                end.latitude,
                end.longitude,
            ) / 1_000.0
        }
    }

    companion object {
        private const val EARTH_RADIUS_METERS = 6_371_000.0
        private const val MIN_SAMPLE_INTERVAL_MS = 15_000L
        private const val MIN_SAMPLE_DISTANCE_METERS = 5.0
        private const val MAX_REASONABLE_JUMP_METERS = 10_000.0
        private const val MAX_JUMP_INTERVAL_MS = 60_000L

        fun distanceMeters(
            startLatitude: Double,
            startLongitude: Double,
            endLatitude: Double,
            endLongitude: Double,
        ): Double {
            val lat1 = Math.toRadians(startLatitude)
            val lat2 = Math.toRadians(endLatitude)
            val deltaLat = Math.toRadians(endLatitude - startLatitude)
            val deltaLon = Math.toRadians(endLongitude - startLongitude)
            val a = sin(deltaLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)
            return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
        }
    }
}

data class DistanceAnalytics(
    val month: YearMonth,
    val monthlyDistanceAverageKm: Double,
    val currentMonthDistanceKm: Double,
    val totalDistanceKm: Double,
    val dailyDistanceAverageKm: Double,
    val activeDays: Int,
    val dailyDistances: List<DailyDistance>,
    val recentLocations: List<LocationHistoryEntity>,
)

data class DailyDistance(
    val date: LocalDate,
    val distanceKm: Double,
)

private fun List<Double>.averageOrZero(): Double = if (isEmpty()) 0.0 else average()
