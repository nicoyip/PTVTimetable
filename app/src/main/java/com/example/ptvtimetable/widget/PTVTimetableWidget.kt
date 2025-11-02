package com.example.ptvtimetable.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.appwidget.cornerRadius
import androidx.glance.unit.ColorProvider
import com.example.ptvtimetable.R
import com.example.ptvtimetable.data.models.Departure
import com.example.ptvtimetable.data.models.Disruption
import java.text.SimpleDateFormat
import java.util.*

class PTVTimetableWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                PTVWidgetContent()
            }
        }
    }

    @Composable
    private fun PTVWidgetContent() {
        val context = LocalContext.current
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

        val routeType = prefs.getString("route_type", "1") ?: "1"
        val routeName = prefs.getString("route_name", "1") ?: "1"
        val fromStop = prefs.getString("from_stop", "") ?: ""
        val toStop = prefs.getString("to_stop", "") ?: ""
        val lastUpdateTime = prefs.getLong("last_update", System.currentTimeMillis())
        val hasDisruptions = prefs.getBoolean("has_disruptions", false)

        val departuresJson = prefs.getString("departures", "[]")

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(getRouteGradient(routeType))
                .cornerRadius(16.dp)
                .padding(12.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) {
                // Header
                WidgetHeader(
                    routeType = routeType,
                    lastUpdate = lastUpdateTime,
                    hasDisruptions = hasDisruptions
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Stop Information
                if (fromStop.isNotEmpty()) {
                    Text(
                        text = fromStop,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color.White)
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Text(
                        text = "to $toStop",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = ColorProvider(Color.White)
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Text(
                        text = "Route $routeName",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = ColorProvider(getRouteColor(routeType))
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(12.dp))

                    // Departures (simplified display from preferences)
                    DeparturesSection()
                } else {
                    Text(
                        text = "Configure widget in app",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = ColorProvider(Color.White)
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun WidgetHeader(
        routeType: String,
        lastUpdate: Long,
        hasDisruptions: Boolean
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transport icon would go here
            Text(
                text = getRouteTypeIcon(routeType),
                style = TextStyle(
                    fontSize = 14.sp,
                    color = ColorProvider(Color.White)
                )
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            Text(
                text = "Updated at ${timeFormat.format(Date(lastUpdate))}",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = ColorProvider(Color.White)
                )
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            if (hasDisruptions) {
                Text(
                    text = "⚠ Disruptions",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = ColorProvider(Color(0xFFF9D748))
                    )
                )
            }
        }
    }

    @Composable
    private fun DeparturesSection() {
        val context = LocalContext.current
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

        // Display up to 3 departures
        repeat(3) { index ->
            val platform = prefs.getString("dep_${index}_platform", null)
            val scheduledTime = prefs.getString("dep_${index}_scheduled", null)
            val estimatedTime = prefs.getString("dep_${index}_estimated", null)
            val isExpress = prefs.getBoolean("dep_${index}_express", false)
            val minutesUntil = prefs.getInt("dep_${index}_minutes", -1)

            if (scheduledTime != null) {
                DepartureRow(
                    platform = platform,
                    scheduledTime = scheduledTime,
                    isExpress = isExpress,
                    minutesUntil = minutesUntil
                )
                Spacer(modifier = GlanceModifier.height(6.dp))
            }
        }
    }

    @Composable
    private fun DepartureRow(
        platform: String?,
        scheduledTime: String,
        isExpress: Boolean,
        minutesUntil: Int
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (platform != null) {
                Text(
                    text = "Platform $platform",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = ColorProvider(Color.Black)
                    )
                )
                Spacer(modifier = GlanceModifier.width(12.dp))
            }

            Text(
                text = "Scheduled $scheduledTime",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = ColorProvider(Color.Black)
                )
            )

            Spacer(modifier = GlanceModifier.width(12.dp))

            if (isExpress) {
                Text(
                    text = "EXPRESS",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = ColorProvider(Color(0xFF88BC41)),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            if (minutesUntil >= 0) {
                val timeText = when {
                    minutesUntil == 0 -> "Now"
                    minutesUntil < 120 -> "$minutesUntil min"
                    else -> ""
                }

                if (timeText.isNotEmpty()) {
                    Text(
                        text = timeText,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = ColorProvider(Color(0xFF88BC41)),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }

    private fun getRouteColor(routeType: String): Color {
        return when (routeType) {
            "0" -> Color(0xFF3070C7) // Train
            "1" -> Color(0xFF88BC41) // Tram
            "2" -> Color(0xFFEF8933) // Bus
            "3" -> Color(0xFF832690) // V/Line
            "4" -> Color(0xFFFFFFFF) // Night Bus
            else -> Color(0xFFFFFFFF)
        }
    }

    private fun getRouteGradient(routeType: String): Color {
        // Simplified gradient - using primary color with dark background
        return Color(0xFF333434)
    }

    private fun getRouteTypeIcon(routeType: String): String {
        return when (routeType) {
            "0", "3" -> "🚆" // Train/V-Line
            "1" -> "🚊" // Tram
            "2", "4" -> "🚌" // Bus
            else -> "🚊"
        }
    }
}
