package com.example.ptvtimetable.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ptvtimetable.data.api.PTVApiClient
import com.example.ptvtimetable.data.repository.PTVRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class PTVWidgetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = PTVRepository()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val prefs =
                applicationContext.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

            // Get configuration
            val routeType =
                prefs.getString("route_type", null) ?: return@withContext Result.success()
            val routeName =
                prefs.getString("route_name", null) ?: return@withContext Result.success()
            val fromStop = prefs.getString("from_stop", null) ?: return@withContext Result.success()
            val toStop = prefs.getString("to_stop", null) ?: return@withContext Result.success()

            // Fetch token from GitHub (as in JS script)
            val token = fetchToken()
            PTVApiClient.setToken(token)

            // Get route info
            val routeResult = repository.getRoute(routeType, routeName)
            val route = routeResult.getOrNull() ?: return@withContext Result.failure()

            // Search stops
            val stopDepResult = repository.searchStop(fromStop, routeType)
            val stopDesResult = repository.searchStop(toStop, routeType)
            val stopDep = stopDepResult.getOrNull() ?: return@withContext Result.failure()
            val stopDes = stopDesResult.getOrNull() ?: return@withContext Result.failure()

            // Get departures
            val departuresResult = repository.getStopDepartures(stopDep.id, route.id, routeType, 5)
            val allDepartures = departuresResult.getOrNull() ?: return@withContext Result.failure()

            // Get disruptions
            val disruptionsResult = repository.getDisruptions(route.id)
            val disruptions = disruptionsResult.getOrNull() ?: emptyList()

            // Determine direction
            val directionId = repository.determineDirection(allDepartures, stopDep.id, stopDes.id)
                ?: return@withContext Result.failure()

            // Filter departures by direction
            val departures = repository.filterDeparturesByDirection(allDepartures, directionId)
                .take(3) // Take top 3

            // Save to preferences
            prefs.edit().apply {
                putLong("last_update", System.currentTimeMillis())
                putBoolean("has_disruptions", disruptions.isNotEmpty())

                // Save departures
                departures.forEachIndexed { index, departure ->
                    putString("dep_${index}_platform", departure.platformNumber)

                    // Format scheduled time
                    val scheduledTime = parseUtcTime(departure.scheduledDepartureUtc)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    putString("dep_${index}_scheduled", timeFormat.format(scheduledTime))

                    // Calculate minutes until departure
                    val estimatedTimeStr =
                        departure.estimatedDepartureUtc ?: departure.scheduledDepartureUtc
                    val estimatedTime = parseUtcTime(estimatedTimeStr)
                    val minutesUntil =
                        ((estimatedTime.time - System.currentTimeMillis()) / 60000).toInt()
                    putInt("dep_${index}_minutes", minutesUntil)

                    val isExpress = (departure.run.expressStopCount ?: 0) > 0
                    putBoolean("dep_${index}_express", isExpress)
                }

                apply()
            }

            // Update widget
            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = manager.getGlanceIds(PTVTimetableWidget::class.java)
            glanceIds.forEach { glanceId ->
                PTVTimetableWidget().update(applicationContext, glanceId)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun fetchToken(): String = withContext(Dispatchers.IO) {
        try {
            val url =
                java.net.URL("https://raw.githubusercontent.com/imchlorine/PTVTimetable/refs/heads/main/token")
            val connection = url.openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            val token = connection.getInputStream().bufferedReader().use { it.readText() }
            token.trim()
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseUtcTime(utcString: String): Date {
        return try {
            val instant = Instant.parse(utcString)
            Date.from(instant)
        } catch (e: Exception) {
            Date()
        }
    }
}
