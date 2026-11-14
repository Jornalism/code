package com.example.campusq.data.model

import androidx.compose.runtime.Immutable

enum class UserRole {
    STUDENT,
    STAFF,
    ADMIN
}

enum class QueueStatus(val displayName: String) {
    WAITING("WAITING"),
    CALLED("CALLED"),
    SERVING("SERVING"),
    COMPLETED("COMPLETED"),
    SKIPPED("SKIPPED"),
    NO_SHOW("NO_SHOW"),
    CANCELLED("CANCELLED"),
    EXPIRED("EXPIRED")
}

enum class AppointmentStatus(val displayName: String) {
    BOOKED("BOOKED"),
    CONFIRMED("CONFIRMED"),
    CHECKED_IN("CHECKED_IN"),
    SERVING("SERVING"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED"),
    NO_SHOW("NO_SHOW")
}

enum class TransactionType {
    QUEUE,
    APPOINTMENT
}

enum class ServiceIconType {
    DOCUMENT_REQUEST,
    ENROLLMENT_VERIFICATION,
    GOOD_MORAL,
    TRANSCRIPT,
    OTHER
}

@Immutable
data class StudentProfile(
    val id: String,
    val fullName: String,
    val studentId: String,
    val email: String,
    val program: String,
    val yearLevel: String
)

@Immutable
data class StaffProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val roleTitle: String = "Registrar Staff",
    val counterName: String = "Window 1",
    val isActive: Boolean = true
)

@Immutable
data class RegistrarService(
    val id: String,
    val name: String,
    val description: String,
    val requirements: List<String>,
    val iconType: ServiceIconType,
    val isAvailable: Boolean = true,
    val isQueueEnabled: Boolean = true,
    val isAppointmentEnabled: Boolean = true,
    val estimatedMinutes: Int = 10,
    val version: Int = 0
)

@Immutable
data class QueueTicket(
    val id: String,
    val ticketNumber: String,
    val serviceId: String,
    val serviceName: String,
    val studentName: String,
    val studentId: String,
    val status: QueueStatus,
    val positionInLine: Int,
    val peopleAhead: Int,
    val estimatedWaitMinutes: Int,
    val timeJoined: String,
    val waitingDurationText: String,
    val startTime: String? = null,
    val completedTime: String? = null,
    val version: Int = 0
)

@Immutable
data class Appointment(
    val id: String,
    val referenceNumber: String,
    val serviceId: String,
    val serviceName: String,
    val studentName: String,
    val studentId: String,
    val dateText: String,
    val timeSlot: String,
    val status: AppointmentStatus,
    val isToday: Boolean = true,
    val version: Int = 0
)

@Immutable
data class TransactionRecord(
    val id: String,
    val referenceNumber: String,
    val type: TransactionType,
    val serviceName: String,
    val studentName: String,
    val dateTimeText: String,
    val queueStatus: QueueStatus? = null,
    val appointmentStatus: AppointmentStatus? = null,
    val ownerId: String = "stu-001",
    val serviceId: String? = null
)

@Immutable
data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false
)

@Immutable
data class ServiceAnalyticsBar(
    val serviceName: String,
    val shortLabel: String,
    val count: Int
)

@Immutable
data class InstitutionSettings(
    val institutionName: String = "CampusQ State University — Office of the University Registrar",
    val operatingDays: String = "Monday – Friday",
    val operatingHours: String = "8:00 AM – 5:00 PM",
    val maxDailyQueueLimit: Int = 150,
    val pushAlertsEnabled: Boolean = true,
    val smsFallbackEnabled: Boolean = false
)
