package com.example.ptvtimetable

import com.example.ptvtimetable.data.models.Departure
import com.example.ptvtimetable.data.models.Route
import com.example.ptvtimetable.data.models.Run
import com.example.ptvtimetable.data.repository.PTVRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * BDD-style unit test for PTVRepository using Kotest framework.
 * Demonstrates behavior-driven development approach with Given-When-Then structure.
 */
class PTVRepositoryBDDTest : BehaviorSpec({

    Given("a list of departures with mixed directions") {
        val repository = PTVRepository()
        val departures = listOf(
            createDeparture(directionId = 5, routeId = 100),
            createDeparture(directionId = 3, routeId = 100),
            createDeparture(directionId = 5, routeId = 100),
            createDeparture(directionId = 7, routeId = 100)
        )

        When("filtering by direction 5") {
            val result = repository.filterDeparturesByDirection(departures, 5)

            Then("should return only departures with direction 5") {
                result shouldHaveSize 2
                result.all { it.directionId == 5 } shouldBe true
            }
        }

        When("filtering by non-existent direction") {
            val result = repository.filterDeparturesByDirection(departures, 99)

            Then("should return empty list") {
                result.shouldBeEmpty()
            }
        }
    }

    Given("departures with a single direction") {
        val repository = PTVRepository()
        val singleDirectionDepartures = listOf(
            createDeparture(directionId = 10),
            createDeparture(directionId = 10),
            createDeparture(directionId = 10)
        )

        When("determining the direction") {
            val result = repository.determineDirection(singleDirectionDepartures, 123, 456)

            Then("should return the direction ID") {
                result.shouldNotBeNull()
                result shouldBe 10
            }
        }
    }

    Given("departures with multiple directions") {
        val repository = PTVRepository()
        val multiDirectionDepartures = listOf(
            createDeparture(directionId = 5),
            createDeparture(directionId = 8),
            createDeparture(directionId = 5),
            createDeparture(directionId = 8)
        )

        When("determining the direction") {
            val result = repository.determineDirection(multiDirectionDepartures, 123, 456)

            Then("should return one of the available directions") {
                result.shouldNotBeNull()
                (result == 5 || result == 8) shouldBe true
            }
        }
    }

    Given("an empty list of departures") {
        val repository = PTVRepository()
        val emptyDepartures = emptyList<Departure>()

        When("determining the direction") {
            val result = repository.determineDirection(emptyDepartures, 123, 456)

            Then("should return null") {
                result.shouldBeNull()
            }
        }

        When("filtering by any direction") {
            val result = repository.filterDeparturesByDirection(emptyDepartures, 5)

            Then("should return empty list") {
                result.shouldBeEmpty()
            }
        }
    }

    Given("departures for a specific route") {
        val repository = PTVRepository()
        val route100Departures = listOf(
            createDeparture(directionId = 1, routeId = 100),
            createDeparture(directionId = 1, routeId = 100),
            createDeparture(directionId = 2, routeId = 100)
        )

        When("filtering by direction 1") {
            val result = repository.filterDeparturesByDirection(route100Departures, 1)

            Then("should maintain all departure properties") {
                result shouldHaveSize 2
                result.forEach { departure ->
                    departure.directionId shouldBe 1
                    departure.route.id shouldBe 100
                    departure.platformNumber.shouldNotBeNull()
                }
            }
        }
    }
})

/**
 * Helper function to create test Departure objects with proper @SerializedName annotations
 */
private fun createDeparture(
    directionId: Int,
    routeId: Int = 1,
    scheduledTime: String = "2024-01-01T10:00:00Z",
    platformNumber: String = "1"
): Departure {
    return Departure(
        scheduledDepartureUtc = scheduledTime,
        estimatedDepartureUtc = null,
        platformNumber = platformNumber,
        directionId = directionId,
        route = Route(
            id = routeId,
            label = "Test Route $routeId",
            shortLabel = "TR$routeId",
            routeType = 0
        ),
        run = Run(
            id = routeId * 10,
            expressStopCount = 0
        )
    )
}
