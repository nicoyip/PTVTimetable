package com.example.ptvtimetable.data.api

import com.example.ptvtimetable.data.models.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * PTV Timetable API v3 Service
 * Official API documentation: https://timetableapi.ptv.vic.gov.au/swagger/ui/index
 */
interface PTVApiService {

    /**
     * Get all routes of a specified route type
     * Endpoint: GET /v3/routes
     */
    @GET("v3/routes")
    suspend fun getRoutes(
        @Query("route_type") routeType: Int? = null
    ): RoutesResponse

    /**
     * Search for stops, routes and myki ticket outlets
     * Endpoint: GET /v3/search/{search_term}
     */
    @GET("v3/search/{search_term}")
    suspend fun searchStop(
        @Path("search_term") searchTerm: String,
        @Query("route_types") routeTypes: List<Int>? = null,
        @Query("latitude") latitude: Float? = null,
        @Query("longitude") longitude: Float? = null,
        @Query("max_distance") maxDistance: Float? = null,
        @Query("include_addresses") includeAddresses: Boolean = false,
        @Query("include_outlets") includeOutlets: Boolean = false,
        @Query("match_stop_by_suburb") matchStopBySuburb: Boolean = true,
        @Query("match_route_by_suburb") matchRouteBySuburb: Boolean = true,
        @Query("match_stop_by_gtfs_stop_id") matchStopByGtfsStopId: Boolean = false
    ): SearchResponse

    /**
     * Get all departures from a stop
     * Endpoint: GET /v3/departures/route_type/{route_type}/stop/{stop_id}
     */
    @GET("v3/departures/route_type/{route_type}/stop/{stop_id}")
    suspend fun getStopDepartures(
        @Path("route_type") routeType: Int,
        @Path("stop_id") stopId: Int,
        @Query("platform_numbers") platformNumbers: List<Int>? = null,
        @Query("direction_id") directionId: Int? = null,
        @Query("look_backwards") lookBackwards: Boolean = false,
        @Query("gtfs") gtfs: Boolean = false,
        @Query("date_utc") dateUtc: String? = null,
        @Query("max_results") maxResults: Int = 5,
        @Query("include_cancelled") includeCancelled: Boolean = false,
        @Query("expand") expand: List<String>? = null
    ): DeparturesResponse

    /**
     * Get stops on a route
     * Endpoint: GET /v3/stops/route/{route_id}/route_type/{route_type}
     */
    @GET("v3/stops/route/{route_id}/route_type/{route_type}")
    suspend fun getStopsOnRoute(
        @Path("route_id") routeId: Int,
        @Path("route_type") routeType: Int,
        @Query("direction_id") directionId: Int? = null,
        @Query("stop_disruptions") stopDisruptions: Boolean = false
    ): StopPatternResponse

    /**
     * Get all disruptions
     * Endpoint: GET /v3/disruptions
     */
    @GET("v3/disruptions")
    suspend fun getDisruptions(
        @Query("route_types") routeTypes: List<Int>? = null,
        @Query("disruption_modes") disruptionModes: List<Int>? = null,
        @Query("disruption_status") disruptionStatus: String? = null
    ): DisruptionsResponse

}
