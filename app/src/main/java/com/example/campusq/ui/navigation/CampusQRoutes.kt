package com.example.campusq.ui.navigation

object CampusQRoutes {
    // Entry & Auth (Screens 01-03 + Demo Role Preview)
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DEMO_PREVIEW_HUB = "demo_preview_hub"

    // Student Role (Screens 04-09)
    const val STUDENT_HOME = "student_home"
    const val STUDENT_SERVICES = "student_services"
    const val STUDENT_QUEUE_STATUS = "student_queue_status"
    const val STUDENT_BOOK_APPOINTMENT = "student_book_appointment"
    const val STUDENT_HISTORY = "student_history"
    const val STUDENT_PROFILE = "student_profile"

    // Staff Role (Screens 10-16)
    const val STAFF_DASHBOARD = "staff_dashboard"
    const val STAFF_QUEUE_MANAGEMENT = "staff_queue_management"
    const val STAFF_TICKET_DETAILS = "staff_ticket_details/{ticketId}"
    const val STAFF_SERVICE_IN_PROGRESS = "staff_service_in_progress/{ticketId}"
    const val STAFF_APPOINTMENTS = "staff_appointments"
    const val STAFF_SERVICE_STATUS = "staff_service_status"
    const val STAFF_PROFILE = "staff_profile"

    fun staffTicketDetailsRoute(ticketId: String) = "staff_ticket_details/$ticketId"
    fun staffServiceInProgressRoute(ticketId: String) = "staff_service_in_progress/$ticketId"

    fun roleHome(route: String): String? = when {
        route.startsWith("student_") -> STUDENT_HOME
        route.startsWith("staff_") -> STAFF_DASHBOARD
        route.startsWith("admin_") -> ADMIN_DASHBOARD
        else -> null
    }

    val roleTabs = setOf(
        STUDENT_HOME, STUDENT_QUEUE_STATUS, STUDENT_BOOK_APPOINTMENT, STUDENT_HISTORY, STUDENT_PROFILE,
        STAFF_DASHBOARD, STAFF_QUEUE_MANAGEMENT, STAFF_APPOINTMENTS, STAFF_SERVICE_STATUS, STAFF_PROFILE,
        ADMIN_DASHBOARD, ADMIN_MANAGE_SERVICES, ADMIN_STAFF_MANAGEMENT, ADMIN_QUEUE_MONITORING,
        ADMIN_APPOINTMENTS, ADMIN_REPORTS, ADMIN_SYSTEM_SETTINGS
    )

    // Admin Role (Screens 17-23)
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_MANAGE_SERVICES = "admin_manage_services"
    const val ADMIN_STAFF_MANAGEMENT = "admin_staff_management"
    const val ADMIN_QUEUE_MONITORING = "admin_queue_monitoring"
    const val ADMIN_APPOINTMENTS = "admin_appointments"
    const val ADMIN_REPORTS = "admin_reports"
    const val ADMIN_SYSTEM_SETTINGS = "admin_system_settings"
}
