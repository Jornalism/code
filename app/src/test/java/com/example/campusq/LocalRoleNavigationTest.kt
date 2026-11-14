package com.example.campusq

import com.example.campusq.data.model.UserRole
import com.example.campusq.ui.navigation.localRoleCanOpen
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalRoleNavigationTest {
    @Test fun studentCannotOpenStaffAdminOrPreview() {
        assertTrue(localRoleCanOpen(UserRole.STUDENT, "student_home"))
        assertFalse(localRoleCanOpen(UserRole.STUDENT, "staff_dashboard"))
        assertFalse(localRoleCanOpen(UserRole.STUDENT, "admin_dashboard"))
        assertFalse(localRoleCanOpen(UserRole.STUDENT, "demo_preview_hub"))
    }
    @Test fun staffCannotOpenAdministrativeOrStudentRoutes() {
        assertTrue(localRoleCanOpen(UserRole.STAFF, "staff_ticket_details/id"))
        assertFalse(localRoleCanOpen(UserRole.STAFF, "admin_manage_services"))
        assertFalse(localRoleCanOpen(UserRole.STAFF, "student_profile"))
    }
    @Test fun administratorCanInspectTicketsWithoutStudentImpersonation() {
        assertTrue(localRoleCanOpen(UserRole.ADMIN, "admin_staff_management"))
        assertTrue(localRoleCanOpen(UserRole.ADMIN, "staff_ticket_details/id"))
        assertFalse(localRoleCanOpen(UserRole.ADMIN, "student_home"))
        assertFalse(localRoleCanOpen(UserRole.ADMIN, "login"))
    }
}
