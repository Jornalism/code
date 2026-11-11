package com.example.campusq

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun campusQKeepsAllTwentyThreeScreensAndTheDemoHub() {
        val routes = com.example.campusq.ui.navigation.CampusQRoutes
        val destinations = routes.roleTabs + setOf(
            routes.SPLASH, routes.LOGIN, routes.REGISTER, routes.DEMO_PREVIEW_HUB,
            routes.STUDENT_SERVICES, routes.STAFF_TICKET_DETAILS, routes.STAFF_SERVICE_IN_PROGRESS
        )
        assertEquals(24, destinations.size)
    }
}