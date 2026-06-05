package com.labpro.nimons360.ui.features.analytics

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.data.model.analytics.LocationHistoryEntity
import com.labpro.nimons360.data.repository.DistanceAnalytics
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalyticsActivity : AppCompatActivity() {
    private val app: MainApplication
        get() = application as MainApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)
        findViewById<View>(R.id.btnAnalyticsBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnExportAnalytics).setOnClickListener {
            exportCsv()
        }
        loadAnalytics()
    }

    private fun loadAnalytics() {
        lifecycleScope.launch {
            val analytics = withContext(Dispatchers.IO) {
                app.analyticsRepository.summary()
            }
            render(analytics)
        }
    }

    private fun render(data: DistanceAnalytics) {
        findViewById<TextView>(R.id.tvMonthlyAverage).text =
            getString(R.string.analytics_km_value, data.monthlyDistanceAverageKm)
        findViewById<TextView>(R.id.tvMonthlyDescription).text =
            getString(R.string.analytics_monthly_description, data.currentMonthDistanceKm)
        findViewById<TextView>(R.id.tvTotalDistance).text =
            getString(R.string.analytics_number_value, data.totalDistanceKm)
        findViewById<TextView>(R.id.tvDailyAverage).text =
            getString(R.string.analytics_number_value, data.dailyDistanceAverageKm)
        findViewById<TextView>(R.id.tvActiveDays).text = data.activeDays.toString()
        findViewById<TextView>(R.id.tvAnalyticsMonth).text =
            data.month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))

        val today = java.time.LocalDate.now().takeIf {
            java.time.YearMonth.from(it) == data.month
        }?.dayOfMonth ?: 0
        findViewById<DailyDistanceChartView>(R.id.dailyDistanceChart)
            .submit(data.dailyDistances, today)
        renderRecent(data.recentLocations)
    }

    private fun renderRecent(items: List<LocationHistoryEntity>) {
        val container = findViewById<LinearLayout>(R.id.recentLocationContainer)
        val empty = findViewById<TextView>(R.id.tvRecentEmpty)
        container.removeAllViews()
        empty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

        items.forEachIndexed { index, item ->
            val row = layoutInflater.inflate(R.layout.item_recent_location, container, false)
            row.findViewById<TextView>(R.id.tvRecentCoordinate).text = String.format(
                Locale.US,
                "%.5f,  %.5f",
                item.latitude,
                item.longitude,
            )
            row.findViewById<TextView>(R.id.tvRecentTime).text = relativeTime(item.recordedAt)
            row.findViewById<View>(R.id.recentDivider).visibility =
                if (index == items.lastIndex) View.GONE else View.VISIBLE
            container.addView(row)
        }
    }

    private fun relativeTime(timestamp: Long): String {
        val elapsedMinutes = ((System.currentTimeMillis() - timestamp) / 60_000L).coerceAtLeast(0)
        return when {
            elapsedMinutes < 1 -> getString(R.string.analytics_just_now)
            elapsedMinutes < 60 -> getString(R.string.analytics_minutes_ago, elapsedMinutes)
            elapsedMinutes < 1_440 -> getString(
                R.string.analytics_hours_minutes_ago,
                elapsedMinutes / 60,
                elapsedMinutes % 60,
            )
            else -> Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd MMM, HH:mm"))
        }
    }

    private fun exportCsv() {
        lifecycleScope.launch {
            val file = withContext(Dispatchers.IO) {
                val summary = app.analyticsRepository.summary()
                val history = app.analyticsRepository.allHistory()
                writeCsv(summary, history)
            }
            shareCsv(file)
        }
    }

    private fun writeCsv(
        analytics: DistanceAnalytics,
        history: List<LocationHistoryEntity>,
    ): File {
        val directory = File(cacheDir, "analytics_exports").apply { mkdirs() }
        val file = File(directory, "nimons360-analytics-${System.currentTimeMillis()}.csv")
        val timestampFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        file.bufferedWriter().use { writer ->
            writer.appendLine("analytics_metric,value")
            writer.appendLine("month,${csv(analytics.month.toString())}")
            writer.appendLine("monthly_distance_average_km,${format(analytics.monthlyDistanceAverageKm)}")
            writer.appendLine("current_month_distance_km,${format(analytics.currentMonthDistanceKm)}")
            writer.appendLine("total_distance_km,${format(analytics.totalDistanceKm)}")
            writer.appendLine("daily_distance_average_km,${format(analytics.dailyDistanceAverageKm)}")
            writer.appendLine("active_days,${analytics.activeDays}")
            writer.appendLine()
            writer.appendLine("daily_distance")
            writer.appendLine("date,distance_km")
            analytics.dailyDistances.forEach {
                writer.appendLine("${it.date},${format(it.distanceKm)}")
            }
            writer.appendLine()
            writer.appendLine("location_history")
            writer.appendLine("recorded_at,latitude,longitude")
            history.forEach {
                val timestamp = Instant.ofEpochMilli(it.recordedAt)
                    .atZone(ZoneId.systemDefault())
                    .format(timestampFormat)
                writer.appendLine("${csv(timestamp)},${it.latitude},${it.longitude}")
            }
        }
        return file
    }

    private fun shareCsv(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(contentResolver, file.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            startActivity(Intent.createChooser(intent, getString(R.string.analytics_export_title)))
        }.onFailure {
            Toast.makeText(this, R.string.analytics_export_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun format(value: Double): String = String.format(Locale.US, "%.3f", value)

    private fun csv(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}
