package com.example.ptvtimetable.data.repository

import com.example.ptvtimetable.data.api.PTVApiClient
import com.example.ptvtimetable.data.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class PTVRepository {

    private val apiService = PTVApiClient.apiService

    suspend fun getRoute(routeType: String, routeName: String): Result<Route> =
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Loading routes from feed: routeType=$routeType, routeName=$routeName")
                val response = apiService.getRoutes(routeType)
                Timber.d("Routes loaded successfully: ${response.routes.size} routes found")
                val routes = response.routes.filter { it.shortLabel == routeName }

                val route = if (routes.size == 1) {
                    routes[0]
                } else {
                    response.routes.find { it.label == routeName }
                }

                route?.let {
                    Timber.d("Route found: id=${it.id}, label=${it.label}")
                    Result.success(it)
                } ?: run {
                    Timber.w("Route not found: $routeName")
                    Result.failure(Exception("Route not found"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading routes from feed")
                Result.failure(e)
            }
        }

    suspend fun searchStop(stopName: String, routeType: String): Result<Stop> =
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Searching stop from feed: stopName=$stopName, routeType=$routeType")
                val response = apiService.searchStop(stopName)
                val stopsData = response.results.stop

                // Handle API's inconsistent response format
                val stops = when (stopsData) {
                    is List<*> -> {
                        (stopsData[0] as? List<*>)?.filterIsInstance<Stop>() ?: emptyList()
                    }

                    is Map<*, *> -> {
                        val typeStops = stopsData[routeType] as? List<*>
                        typeStops?.filterIsInstance<Stop>() ?: emptyList()
                    }

                    else -> emptyList()
                }

                Timber.d("Stop search completed: ${stops.size} stops found")
                val stop = stops.find { it.label == stopName } ?: stops.firstOrNull()
                stop?.let {
                    Timber.d("Stop found: id=${it.id}, label=${it.label}")
                    Result.success(it)
                } ?: run {
                    Timber.w("Stop not found: $stopName")
                    Result.failure(Exception("Stop not found"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error searching stop from feed")
                Result.failure(e)
            }
        }

    suspend fun getStopDepartures(
        stopId: Int,
        routeId: Int,
        routeType: String,
        maxResults: Int = 5
    ): Result<List<Departure>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Loading departures from feed: stopId=$stopId, routeId=$routeId, routeType=$routeType, maxResults=$maxResults")
            val response = apiService.getStopServices(stopId, routeId, routeType, maxResults)
            val filtered = response.departures.filter { it.route.id == routeId }
            Timber.d("Departures loaded successfully: ${filtered.size} departures found")
            Result.success(filtered)
        } catch (e: Exception) {
            Timber.e(e, "Error loading departures from feed")
            Result.failure(e)
        }
    }

    suspend fun getDisruptions(routeId: Int): Result<List<Disruption>> =
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Loading disruptions from feed: routeId=$routeId")
                val response = apiService.getDisruptions()
                val filtered = response.disruptions.filter {
                    it.routeIds.contains(routeId) && it.kind == "Planned Works"
                }
                Timber.d("Disruptions loaded successfully: ${filtered.size} planned works found")
                Result.success(filtered)
            } catch (e: Exception) {
                Timber.e(e, "Error loading disruptions from feed")
                Result.failure(e)
            }
        }

    suspend fun getRouteStops(
        routeType: String,
        routeId: Int,
        directionId: Int
    ): Result<List<Stop>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Loading route stops from feed: routeType=$routeType, routeId=$routeId, directionId=$directionId")
            val response = apiService.getRouteStops(routeType, routeId, directionId)
            Timber.d("Route stops loaded successfully: ${response.stopsPattern.size} stops found")
            Result.success(response.stopsPattern)
        } catch (e: Exception) {
            Timber.e(e, "Error loading route stops from feed")
            Result.failure(e)
        }
    }

    fun determineDirection(
        departures: List<Departure>,
        stopDepId: Int,
        stopDesId: Int
    ): Int? {
        val groupedByDirection = departures.groupBy { it.directionId }
        val keys = groupedByDirection.keys.toList()

        return if (keys.size == 1) {
            keys[0]
        } else {
            // Would need to call getRouteStops to determine correct direction
            // For now, return first direction
            keys.firstOrNull()
        }
    }

    fun filterDeparturesByDirection(
        departures: List<Departure>,
        directionId: Int
    ): List<Departure> {
        return departures.filter { it.directionId == directionId }
    }
}
