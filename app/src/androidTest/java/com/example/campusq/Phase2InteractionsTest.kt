package com.example.campusq

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBack
import com.example.campusq.data.demo.DemoRepository
import com.example.campusq.data.model.QueueStatus
import com.example.campusq.ui.navigation.CampusQApp
import com.example.campusq.ui.student.BookAppointmentScreen
import com.example.campusq.ui.staff.StaffTicketDetailsScreen
import com.example.campusq.ui.theme.CampusQTheme
import com.example.campusq.viewmodel.CampusQViewModel
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class Phase2InteractionsTest {
    @get:Rule val compose = createComposeRule()
    private val repository = DemoRepository(Clock.fixed(Instant.parse("2026-11-09T00:00:00Z"), ZoneId.of("Asia/Manila")))

    private fun launch() {
        val vm = CampusQViewModel(repository)
        compose.setContent { CampusQTheme { CampusQApp(vm) } }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Welcome Back!").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun click(text: String) {
        val node = compose.onAllNodesWithText(text).onLast()
        if (!node.isDisplayed()) node.performScrollTo()
        node.performClick()
    }

    @Test fun disabledServicesShowEmptyBookingAndDisableConfirmation() {
        val disabledServices = repository.services.value.map { it.copy(isAvailable = false) }
        compose.setContent {
            CampusQTheme {
                BookAppointmentScreen(
                    services = disabledServices, preselectedService = null,
                    onNavigateBottom = {}, onConfirmBooking = { _, _, _ -> error("Disabled booking accepted") }
                )
            }
        }
        compose.onNodeWithText("No Appointments Available").assertIsDisplayed()
        compose.onNodeWithText("Confirm Appointment").performScrollTo().assertIsNotEnabled()
    }

    @Test fun missingTicketShowsNotFoundWithoutAnActionableFallback() {
        compose.setContent { CampusQTheme {
            StaffTicketDetailsScreen(ticket = null, onBackClick = {}, onUpdateStatus = { _, _ -> error("Unknown ticket updated") }, onNavigateToServiceInProgress = {})
        } }
        compose.onNodeWithText("Ticket Not Found").assertIsDisplayed()
        compose.onNodeWithText("Call Ticket").assertDoesNotExist()
    }

    @Test fun loginValidationAndRegistrationBack() {
        launch()
        click("Log In")
        compose.onNodeWithText("Enter a valid school email address").assertIsDisplayed()
        compose.onNodeWithText("Password must be at least 6 characters").assertIsDisplayed()
        click("Register")
        compose.onNodeWithText("Create Account").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText("Welcome Back!").assertIsDisplayed()
    }

    @Test fun studentTabBackReturnsHomeAndExitClearsRoleHistory() {
        launch()
        click("Student")
        click("History")
        pressBack()
        compose.onNodeWithText("Quick Actions").assertIsDisplayed()
        click("Profile")
        click("Logout")
        compose.onNodeWithText("Exit Demo").performClick()
        compose.onNodeWithText("CampusQ UI Preview").assertIsDisplayed()
        click("Registrar Staff Interface")
        compose.onNodeWithText("Registrar Dashboard").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText("CampusQ UI Preview").assertIsDisplayed()
    }

    @Test fun queueCancelJoinAndStaffProcessingShareOneState() {
        repository.saveStaffMember("stf-001", "Maria Santos", "maria.santos@campusq.edu.ph", "Window 4", true)
        launch()
        click("Student")
        click("Queue")
        click("Cancel Ticket")
        compose.onAllNodesWithText("Cancel Ticket").onLast().performClick()
        compose.onNodeWithText("No Active Queue Ticket").assertIsDisplayed()
        click("Browse Registrar Services")
        compose.onNodeWithText("Document Request").performClick()
        compose.onNodeWithText("Join Queue (Demo)").performClick()
        compose.waitForIdle()
        val id = requireNotNull(repository.studentTicketId.value)
        assertEquals("R-011", repository.findTicket(id)?.ticketNumber)
        click("Profile")
        click("Switch Role Preview")
        click("Registrar Staff Interface")
        click("Queue")
        compose.onNodeWithText("R-011").performClick()
        click("Call Ticket")
        compose.onNodeWithText("Confirm").performClick()
        click("Start Service")
        compose.onNodeWithText("Service in Progress").assertIsDisplayed()
        compose.onNodeWithText("Window 4 (Registrar)").assertIsDisplayed()
        click("Complete Service")
        compose.onAllNodesWithText("Complete Service").onLast().performClick()
        compose.waitForIdle()
        assertEquals(QueueStatus.COMPLETED, repository.findTicket(id)?.status)
        assertEquals(QueueStatus.COMPLETED, repository.transactions.value.single { it.referenceNumber == "R-011" }.queueStatus)
    }

    @Test fun staffAppointmentLifecycleAndProfileDialogs() {
        launch()
        click("Staff")
        click("Appointments")
        click("Manage")
        click("Confirm")
        click("Manage")
        click("Check In")
        click("Manage")
        click("Start Service")
        click("Manage")
        click("Complete")
        compose.waitForIdle()
        assertEquals(com.example.campusq.data.model.AppointmentStatus.COMPLETED,
            repository.appointments.value.first { it.id == "apt-a002" }.status)
        click("History")
        compose.onNodeWithText("Service Status").assertIsDisplayed()
        click("Profile")
        click("Account Settings")
        compose.onNodeWithText("Staff Account Settings (Demo)").assertIsDisplayed()
        click("Close")
    }

    @Test fun adminDrawerReachesQueuesAppointmentsAndSettings() {
        launch()
        click("Admin")
        compose.onNodeWithContentDescription("Open Menu").performClick()
        click("Queue Monitoring")
        compose.onNodeWithText("Shared Local Queue State").assertIsDisplayed()
        compose.onNodeWithContentDescription("Open Menu").performClick()
        click("Appointments")
        compose.onNodeWithContentDescription("Open Menu").performClick()
        click("System Settings")
        click("Institution Information")
        compose.onNodeWithText("Institution Information (Demo)").assertIsDisplayed()
        click("Cancel")
        pressBack()
        compose.onAllNodesWithText("Admin Dashboard").onFirst().assertIsDisplayed()
    }

    @Test fun studentProfileNotificationsAndBookingHistory() {
        launch()
        click("Student")
        click("Notifications")
        click("Mark all read")
        compose.waitForIdle()
        assertTrue(repository.notifications.value.all { it.isRead })
        pressBack()
        click("Profile")
        click("Personal Information")
        compose.onNodeWithText("Full Name").performTextReplacement("Al")
        click("Save Demo Info")
        compose.onNodeWithText("Enter your full name.").assertIsDisplayed()
        compose.onNodeWithText("Full Name").performTextReplacement("Juan Campus Tester")
        click("Save Demo Info")
        compose.waitForIdle()
        assertEquals("Juan Campus Tester", repository.studentProfile.value.fullName)
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Demo student profile updated locally.").fetchSemanticsNodes().isEmpty()
        }
        click("Appointments")
        compose.onNodeWithText("Confirm Appointment").performScrollTo().assertIsEnabled()
        click("Confirm Appointment")
        click("Confirm Booking")
        compose.onNodeWithText("Transaction History").assertIsDisplayed()
        assertEquals("Juan Campus Tester", repository.appointments.value.single { it.referenceNumber == "A-005" }.studentName)
    }

    @Test fun administratorAddsAndDisablesStaff() {
        launch()
        click("Admin")
        click("Staff")
        click("Add Staff")
        compose.onNodeWithText("Full Name").performTextInput("Test Registrar")
        compose.onNodeWithText("Institutional Email").performTextInput("test.registrar@campusq.edu.ph")
        click("Save Staff Entry (Demo)")
        compose.waitForIdle()
        assertEquals(5, repository.staffMembers.value.size)
        compose.onAllNodesWithContentDescription("Staff Options").onLast().performClick()
        click("Set Inactive")
        click("Set Inactive")
        compose.waitForIdle()
        assertFalse(repository.staffMembers.value.last().isActive)
    }

    @Test fun administratorSavesSettingsAndChangesReportPeriod() {
        launch()
        click("Admin")
        compose.onNodeWithContentDescription("Open Menu").performClick()
        click("System Settings")
        click("Institution Information")
        compose.onNodeWithText("Institution / Office Name").performTextReplacement("CampusQ Test Registrar")
        click("Save Demo Setting")
        compose.waitForIdle()
        assertEquals("CampusQ Test Registrar", repository.institutionSettings.value.institutionName)
        click("Reports")
        click("This Week (Demo)")
        click("This Month (Demo)")
        compose.onNodeWithText("This Month (Demo)").assertIsDisplayed()
    }

    @Test fun adminServiceEditAndDrawerNavigation() {
        launch()
        click("Admin")
        click("Services")
        compose.onNodeWithText("Document Request").performClick()
        compose.onNodeWithText("Service Name").performTextInput(" Copy")
        click("Save Service (Demo)")
        compose.waitForIdle()
        assertTrue(repository.services.value.first().name.contains("Copy"))
        click("Staff")
        compose.onAllNodesWithText("Staff Management").onFirst().assertIsDisplayed()
        click("Reports")
        compose.onAllNodesWithText("Reports").onFirst().assertIsDisplayed()
        pressBack()
        compose.onAllNodesWithText("Admin Dashboard").onFirst().assertIsDisplayed()
    }
}
