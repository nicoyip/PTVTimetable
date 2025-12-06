package com.example.ptvtimetable.data.models

import com.google.gson.annotations.SerializedName

data class Stop(
    @SerializedName("stop_id")
    val id: Int,
    @SerializedName("stop_name")
    val label: String?,
    @SerializedName("stop_landmark")
    val name: String?,
    @SerializedName("route_type")
    val routeType: Int
)

data class Route(
    @SerializedName("route_id")
    val id: Int,
    @SerializedName("route_name")
    val label: String?,
    @SerializedName("route_number")
    val shortLabel: String?,
    @SerializedName("route_type")
    val routeType: Int
)

data class Run(
    @SerializedName("id")
    val id: Int,
    @SerializedName("express_stop_count")
    val expressStopCount: Int?
)

data class Departure(
    @SerializedName("scheduled_departure_utc")
    val scheduledDepartureUtc: String,
    @SerializedName("estimated_departure_utc")
    val estimatedDepartureUtc: String?,
    @SerializedName("platform_number")
    val platformNumber: String?,
    @SerializedName("direction_id")
    val directionId: Int,
    @SerializedName("route")
    val route: Route,
    @SerializedName("run")
    val run: Run
)

data class Disruption(
    @SerializedName("id")
    val id: Int,
    @SerializedName("kind")
    val kind: String,
    @SerializedName("route_ids")
    val routeIds: List<Int>,
    @SerializedName("title")
    val title: String?,
    @SerializedName("link")
    val link: String?
)

data class SearchResponse(
    @SerializedName("results")
    val results: SearchResults
)

data class SearchResults(
    @SerializedName("stop")
    val stop: Any // Can be List or Map depending on API response
)

data class RoutesResponse(
    @SerializedName("routes")
    val routes: List<Route>
)

data class DeparturesResponse(
    @SerializedName("departures")
    val departures: List<Departure>
)

data class DisruptionsResponse(
    @SerializedName("disruptions")
    val disruptions: List<Disruption>
)

data class StopPatternResponse(
    @SerializedName("stops_pattern")
    val stopsPattern: List<Stop>
)

data class WidgetConfig(
    val routeType: String,
    val routeName: String,
    val fromStop: String,
    val toStop: String
)
