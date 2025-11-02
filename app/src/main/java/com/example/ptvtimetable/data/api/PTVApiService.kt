package com.example.ptvtimetable.data.api

import com.example.ptvtimetable.data.models.*
import retrofit2.http.GET
import retrofit2.http.Query

interface PTVApiService {

    @GET("routes")
    suspend fun getRoutes(
        @Query("route_type") routeType: String
    ): RoutesResponse

    @GET("search")
    suspend fun searchStop(
        @Query("term") term: String,
        @Query("mode") mode: String = "home"
    ): SearchResponse

    @GET("stop-services")
    suspend fun getStopServices(
        @Query("stop_id") stopId: Int,
        @Query("route_id") routeId: Int,
        @Query("mode_id") modeId: String,
        @Query("max_results") maxResults: Int = 5,
        @Query("look_backwards") lookBackwards: Boolean = false
    ): DeparturesResponse

    @GET("route-stops")
    suspend fun getRouteStops(
        @Query("route_type") routeType: String,
        @Query("route_id") routeId: Int,
        @Query("direction_id") directionId: Int
    ): StopPatternResponse

    @GET("disruptions")
    suspend fun getDisruptions(): DisruptionsResponse
}
