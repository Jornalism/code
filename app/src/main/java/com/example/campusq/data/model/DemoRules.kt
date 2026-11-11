package com.example.campusq.data.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val QueueStatus.isActive: Boolean
    get() = this in setOf(QueueStatus.WAITING, QueueStatus.CALLED, QueueStatus.SERVING, QueueStatus.SKIPPED)

fun QueueStatus.canTransitionTo(next: QueueStatus): Boolean = next in when (this) {
    QueueStatus.WAITING -> setOf(QueueStatus.CALLED, QueueStatus.CANCELLED, QueueStatus.EXPIRED)
    QueueStatus.CALLED -> setOf(QueueStatus.SERVING, QueueStatus.SKIPPED, QueueStatus.NO_SHOW, QueueStatus.CANCELLED)
    QueueStatus.SERVING -> setOf(QueueStatus.COMPLETED, QueueStatus.NO_SHOW)
    QueueStatus.SKIPPED -> setOf(QueueStatus.CALLED, QueueStatus.CANCELLED, QueueStatus.EXPIRED)
    else -> emptySet()
}

val AppointmentStatus.canEdit: Boolean
    get() = this == AppointmentStatus.BOOKED || this == AppointmentStatus.CONFIRMED

fun AppointmentStatus.canTransitionTo(next: AppointmentStatus): Boolean = next in when (this) {
    AppointmentStatus.BOOKED -> setOf(AppointmentStatus.CONFIRMED, AppointmentStatus.CHECKED_IN, AppointmentStatus.CANCELLED)
    AppointmentStatus.CONFIRMED -> setOf(AppointmentStatus.CHECKED_IN, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)
    AppointmentStatus.CHECKED_IN -> setOf(AppointmentStatus.SERVING, AppointmentStatus.NO_SHOW)
    AppointmentStatus.SERVING -> setOf(AppointmentStatus.COMPLETED)
    else -> emptySet()
}

object DemoBookingRules {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US)
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    val timeSlots = listOf("9:00 AM", "10:00 AM", "11:00 AM", "1:00 PM", "2:00 PM")

    fun parseDate(text: String): LocalDate = try {
        LocalDate.parse(text, dateFormatter).also {
            require(it.format(dateFormatter) == text) { "Choose a valid appointment date." }
        }
    } catch (_: java.time.format.DateTimeParseException) {
        throw IllegalArgumentException("Choose a valid appointment date.")
    }

    fun validateSchedule(dateText: String, slot: String, today: LocalDate, now: LocalTime): LocalDate {
        val date = parseDate(dateText)
        require(!date.isBefore(today)) { "Choose today or a future date." }
        require(date.dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) { "Appointments are available Monday to Friday." }
        require(slot in timeSlots) { "Choose an available appointment time." }
        require(date != today || LocalTime.parse(slot, timeFormatter).isAfter(now)) { "Choose a future time slot." }
        return date
    }
}
