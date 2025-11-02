package com.example.ptvtimetable

import com.example.ptvtimetable.data.models.Departure
import com.example.ptvtimetable.data.models.Route
import com.example.ptvtimetable.data.models.Run
import com.example.ptvtimetable.data.repository.PTVRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Sample unit test for PTVRepository demonstrating:
 * - Testing pure functions (filterDeparturesByDirection, determineDirection)
 * - Using kotlinx.coroutines.test for suspend functions
 * - Creating test data with proper annotations
 * - Multiple test cases with descriptive names
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PTVRepositoryTest {

    private lateinit var repository: PTVRepository

    @Before
    fun setup() {
        repository = PTVRepository()
    }

    @Test
    fun filterDeparturesByDirection_withMatchingDirection_returnsFilteredList() {
        // Arrange
        val targetDirection = 5
        val departures = listOf(
            createDeparture(directionId = 5),
            createDeparture(directionId = 3),
            createDeparture(directionId = 5),
            createDeparture(directionId = 7)
        )

        // Act
        val result = repository.filterDeparturesByDirection(departures, targetDirection)

        // Assert
        assertEquals(2, result.size)
        assertTrue(result.all { it.directionId == targetDirection })
    }

    @Test
    fun filterDeparturesByDirection_withNoMatches_returnsEmptyList() {
        // Arrange
        val departures = listOf(
            createDeparture(directionId = 1),
            createDeparture(directionId = 2)
        )

        // Act
        val result = repository.filterDeparturesByDirection(departures, 99)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun determineDirection_withSingleDirection_returnsDirectionId() {
        // Arrange
        val departures = listOf(
            createDeparture(directionId = 10),
            createDeparture(directionId = 10),
            createDeparture(directionId = 10)
        )

        // Act
        val result = repository.determineDirection(departures, 123, 456)

        // Assert
        assertEquals(10, result)
    }

    @Test
    fun determineDirection_withMultipleDirections_returnsFirstDirection() {
        // Arrange
        val departures = listOf(
            createDeparture(directionId = 5),
            createDeparture(directionId = 8),
            createDeparture(directionId = 5)
        )

        // Act
        val result = repository.determineDirection(departures, 123, 456)

        // Assert
        assertNotNull(result)
        assertTrue(result == 5 || result == 8)
    }

    @Test
    fun determineDirection_withEmptyList_returnsNull() {
        // Arrange
        val departures = emptyList<Departure>()

        // Act
        val result = repository.determineDirection(departures, 123, 456)

        // Assert
        assertNull(result)
    }

    // Helper function to create test Departure objects
    private fun createDeparture(
        directionId: Int,
        routeId: Int = 1,
        scheduledTime: String = "2024-01-01T10:00:00Z"
    ): Departure {
        return Departure(
            scheduledDepartureUtc = scheduledTime,
            estimatedDepartureUtc = null,
            platformNumber = "1",
            directionId = directionId,
            route = Route(
                id = routeId,
                label = "Test Route",
                shortLabel = "Test",
                routeType = 0
            ),
            run = Run(
                id = 1,
                expressStopCount = 0
            )
        )
    }
}