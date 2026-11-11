package com.example.campusq

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.campusq.data.demo.DemoRepository
import com.example.campusq.data.model.AppointmentStatus
import com.example.campusq.data.model.DemoBookingRules
import com.example.campusq.ui.student.BookAppointmentScreen
import com.example.campusq.ui.auth.LoginScreen
import com.example.campusq.ui.theme.CampusQTheme
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class Phase2AppointmentsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun bookingReschedulingAndCancellationUpdateVisibleDemoState() {
        val repo = DemoRepository()
        compose.setContent { CampusQTheme {
            BookAppointmentScreen(
                services = repo.services.collectAsState().value, preselectedService = null,
                onNavigateBottom = {},
                onConfirmBooking = { service, date, time -> repo.bookStudentAppointment(service, date, time) },
                appointments = repo.appointments.collectAsState().value.filter { it.referenceNumber == "A-005" },
                onCancelAppointment = repo::cancelStudentAppointment,
                onRescheduleAppointment = { id, service, date, time -> repo.rescheduleStudentAppointment(id, service, date, time); true }
            )
        } }
        compose.onNodeWithText("Confirm Appointment").performScrollTo().performClick()
        compose.onNodeWithText("Confirm Booking").performClick()
        compose.waitForIdle()
        val booked = repo.appointments.value.single { it.referenceNumber == "A-005" }
        compose.onNodeWithText("Reschedule").performScrollTo().performClick()
        var next = DemoBookingRules.parseDate(booked.dateText).plusDays(1)
        while (next.dayOfWeek == DayOfWeek.SATURDAY || next.dayOfWeek == DayOfWeek.SUNDAY) next = next.plusDays(1)
        if (YearMonth.from(next) != YearMonth.from(DemoBookingRules.parseDate(booked.dateText))) {
            compose.onNodeWithContentDescription("Next Month").performScrollTo().performClick()
        }
        compose.onNodeWithContentDescription(next.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US))).performScrollTo().performClick()
        compose.onNodeWithText("Confirm Rescheduling").performScrollTo().performClick()
        compose.onNodeWithText("Confirm Booking").performClick()
        compose.waitForIdle()
        assertEquals(next.format(DemoBookingRules.dateFormatter), repo.appointments.value.single { it.id == booked.id }.dateText)
        compose.onNodeWithText("Cancel Appointment").performScrollTo().performClick()
        compose.onAllNodesWithText("Cancel Appointment").onLast().performClick()
        compose.waitForIdle()
        assertEquals(AppointmentStatus.CANCELLED, repo.appointments.value.single { it.id == booked.id }.status)
        assertEquals(AppointmentStatus.CANCELLED, repo.transactions.value.single { it.referenceNumber == booked.referenceNumber }.appointmentStatus)
    }

    @Test fun loginInputSurvivesSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { CampusQTheme {
            LoginScreen(onNavigateToRegister = {}, onNavigateToDemoHub = {}, onOpenStudentDemo = {}, onOpenStaffDemo = {}, onOpenAdminDemo = {})
        } }
        compose.onNodeWithText("Email Address").performTextInput("student@campusq.edu.ph")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("student@campusq.edu.ph").assertIsDisplayed()
    }
}
