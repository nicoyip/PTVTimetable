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

    suspend fun getAllRoutes(routeType: Int?): Result<RoutesResponse> =
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Loading all routes: routeType=$routeType")
                val response = apiService.getRoutes(routeType)
                Timber.d("Routes loaded successfully: ${response.routes.size} routes found")
                Result.success(response)
            } catch (e: Exception) {
                Timber.e(e, "Error loading all routes")
                Result.failure(e)
            }
        }

    suspend fun getStopsOnRoute(
        routeId: Int,
        routeType: Int,
        directionId: Int? = null
    ): Result<StopPatternResponse> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Loading stops on route: routeId=$routeId, routeType=$routeType, directionId=$directionId")
            val response = apiService.getStopsOnRoute(
                routeId = routeId,
                routeType = routeType,
                directionId = directionId
            )
            Timber.d("Stops loaded successfully: ${response.stopsPattern.size} stops found")
            Result.success(response)
        } catch (e: Exception) {
            Timber.e(e, "Error loading stops on route")
            Result.failure(e)
        }
    }

    suspend fun getRoute(routeType: String, routeName: String): Result<Route> =
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Loading routes from feed: routeType=$routeType, routeName=$routeName")
                // Convert string route type to int
                val routeTypeInt = routeType.toIntOrNull()
                val response = apiService.getRoutes(routeTypeInt)
                Timber.d("Routes loaded successfully: ${response.routes.size} routes found")

                // Search by route_name (label field) - exact match first, then case-insensitive
                val route = response.routes.find { it.label == routeName }
                    ?: response.routes.find {
                        it.label?.equals(
                            routeName,
                            ignoreCase = true
                        ) == true
                    }

                route?.let {
                    Timber.d("Route found: id=${it.id}, routeName=${it.label}, routeNumber=${it.shortLabel}")
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
                // Convert route type to int list for filtering
                val routeTypeInt = routeType.toIntOrNull()
                val routeTypes = routeTypeInt?.let { listOf(it) }

                val response = apiService.searchStop(
                    searchTerm = stopName,
                    routeTypes = routeTypes
                )
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
                val stop = stops.find { it.label?.equals(stopName, ignoreCase = true) == true } ?: stops.firstOrNull()
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
            // Convert route type to int
            val routeTypeInt = routeType.toIntOrNull() ?: 0
            val response = apiService.getStopDepartures(
                routeType = routeTypeInt,
                stopId = stopId,
                maxResults = maxResults
            )
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
            // Convert route type to int
            val routeTypeInt = routeType.toIntOrNull() ?: 0
            val response = apiService.getStopsOnRoute(
                routeId = routeId,
                routeType = routeTypeInt,
                directionId = directionId
            )
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
