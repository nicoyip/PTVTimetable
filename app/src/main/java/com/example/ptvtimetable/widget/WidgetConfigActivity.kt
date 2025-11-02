package com.example.ptvtimetable.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ptvtimetable.R
import com.example.ptvtimetable.databinding.ActivityWidgetConfigBinding
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WidgetConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetConfigBinding
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set result to canceled initially
        setResult(Activity.RESULT_CANCELED)

        binding = ActivityWidgetConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get widget ID
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setupViews()
    }

    private fun setupViews() {
        // Setup route type spinner
        val routeTypes = arrayOf("Train", "Tram", "Bus", "V/Line", "Night Bus")
        val routeTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, routeTypes)
        routeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRouteType.adapter = routeTypeAdapter
        binding.spinnerRouteType.setSelection(1) // Default to Tram

        binding.btnSave.setOnClickListener {
            saveConfiguration()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveConfiguration() {
        val routeType = when (binding.spinnerRouteType.selectedItemPosition) {
            0 -> "0" // Train
            1 -> "1" // Tram
            2 -> "2" // Bus
            3 -> "3" // V/Line
            4 -> "4" // Night Bus
            else -> "1"
        }

        val routeName = binding.editRouteName.text.toString()
        val fromStop = binding.editFromStop.text.toString()
        val toStop = binding.editToStop.text.toString()

        if (routeName.isEmpty() || fromStop.isEmpty() || toStop.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Save to preferences
        val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("route_type", routeType)
            putString("route_name", routeName)
            putString("from_stop", fromStop)
            putString("to_stop", toStop)
            apply()
        }

        // Schedule periodic widget updates
        scheduleWidgetUpdates()

        // Update widget immediately
        lifecycleScope.launch {
            // Trigger initial update via WorkManager
            WorkManager.getInstance(applicationContext).enqueue(
                androidx.work.OneTimeWorkRequestBuilder<PTVWidgetWorker>().build()
            )
        }

        // Return success
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    private fun scheduleWidgetUpdates() {
        val workRequest = PeriodicWorkRequestBuilder<PTVWidgetWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "ptv_widget_update",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}
