package com.example.campusq.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.campusq.data.model.UserRole

val LocalAdminOverview = staticCompositionLocalOf<() -> Unit> { {} }
val LocalServerMode = staticCompositionLocalOf { false }

fun localRoleCanOpen(role: UserRole, route: String): Boolean = when (role) {
    UserRole.STUDENT -> route.startsWith("student_")
    UserRole.STAFF -> route.startsWith("staff_")
    UserRole.ADMIN -> route.startsWith("admin_") || route.startsWith("staff_ticket_details/") || route.startsWith("staff_service_in_progress/")
}
