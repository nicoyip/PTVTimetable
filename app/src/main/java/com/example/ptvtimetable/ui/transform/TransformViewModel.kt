package com.example.ptvtimetable.ui.transform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ptvtimetable.data.models.Departure
import com.example.ptvtimetable.data.repository.PTVRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

data class DepartureUiState(
    val isLoading: Boolean = false,
    val departures: List<DepartureItem> = emptyList(),
    val error: String? = null,
    val routeName: String = "",
    val fromStop: String = "",
    val toStop: String = ""
)

data class DepartureItem(
    val scheduledTime: String,
    val minutesUntil: String,
    val platform: String?,
    val isExpress: Boolean,
    val routeLabel: String,
    val routeType: Int
)

class TransformViewModel : ViewModel() {
    private val repository = PTVRepository()

    private val _uiState = MutableStateFlow(DepartureUiState())
    val uiState: StateFlow<DepartureUiState> = _uiState.asStateFlow()

    fun loadDepartures(
        routeType: String,
        routeName: String,
        fromStop: String,
        toStop: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                routeName = routeName,
                fromStop = fromStop,
                toStop = toStop
            )

            try {
                // Step 1: Get route
                val routeResult = repository.getRoute(routeType, routeName)
                if (routeResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Route not found: ${routeResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }
                val route = routeResult.getOrNull()!!

                // Step 2: Search for departure stop
                val stopResult = repository.searchStop(fromStop, routeType)
                if (stopResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Stop not found: ${stopResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }
                val stop = stopResult.getOrNull()!!

                // Step 3: Get departures
                val departuresResult = repository.getStopDepartures(
                    stopId = stop.id,
                    routeId = route.id,
                    routeType = routeType,
                    maxResults = 10
                )

                if (departuresResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load departures: ${departuresResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }

                val departures = departuresResult.getOrNull()!!
                val departureItems = departures.map { it.toDepartureItem() }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    departures = departureItems,
                    error = null
                )

                Timber.d("Successfully loaded ${departureItems.size} departures")
            } catch (e: Exception) {
                Timber.e(e, "Error loading departures")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    private fun Departure.toDepartureItem(): DepartureItem {
        val scheduledTime = formatTime(scheduledDepartureUtc)
        val minutesUntil = calculateMinutesUntil(
            estimatedDepartureUtc ?: scheduledDepartureUtc
        )
        val isExpress = run.expressStopCount?.let { it > 0 } ?: false

        return DepartureItem(
            scheduledTime = scheduledTime,
            minutesUntil = minutesUntil,
            platform = platformNumber,
            isExpress = isExpress,
            routeLabel = route.label ?: "Unknown",  // Use route_name (label field)
            routeType = route.routeType
        )
    }

    private fun formatTime(utcTime: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(utcTime)
            val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
            formatter.format(date!!)
        } catch (e: Exception) {
            Timber.e(e, "Error formatting time: $utcTime")
            "N/A"
        }
    }

    private fun calculateMinutesUntil(utcTime: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val departureDate = parser.parse(utcTime)
            val now = Date()
            val diffMillis = departureDate!!.time - now.time
            val minutes = (diffMillis / 1000 / 60).toInt()

            when {
                minutes < 0 -> "Departed"
                minutes == 0 -> "Now"
                minutes == 1 -> "1 min"
                else -> "$minutes mins"
            }
        } catch (e: Exception) {
            Timber.e(e, "Error calculating minutes until: $utcTime")
            "N/A"
        }
    }
}