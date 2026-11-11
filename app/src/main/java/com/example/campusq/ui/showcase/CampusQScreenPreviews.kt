package com.example.campusq.ui.showcase

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.campusq.data.demo.DemoRepository
import com.example.campusq.data.model.QueueStatus
import com.example.campusq.ui.theme.CampusQTheme
import com.example.campusq.ui.auth.SplashScreen
import com.example.campusq.ui.auth.LoginScreen
import com.example.campusq.ui.auth.RegistrationScreen
import com.example.campusq.ui.auth.DemoRolePreviewHubScreen
import com.example.campusq.ui.student.StudentHomeDashboardScreen
import com.example.campusq.ui.student.RegistrarServicesScreen
import com.example.campusq.ui.student.QueueStatusScreen
import com.example.campusq.ui.student.BookAppointmentScreen
import com.example.campusq.ui.student.TransactionHistoryScreen
import com.example.campusq.ui.student.StudentProfileScreen
import com.example.campusq.ui.staff.StaffDashboardScreen
import com.example.campusq.ui.staff.StaffQueueManagementScreen
import com.example.campusq.ui.staff.StaffTicketDetailsScreen
import com.example.campusq.ui.staff.ServiceInProgressScreen
import com.example.campusq.ui.staff.StaffAppointmentManagementScreen
import com.example.campusq.ui.staff.StaffServiceStatusScreen
import com.example.campusq.ui.staff.StaffProfileScreen
import com.example.campusq.ui.admin.AdminDashboardScreen
import com.example.campusq.ui.admin.AdminManageServicesScreen
import com.example.campusq.ui.admin.AdminStaffManagementScreen
import com.example.campusq.ui.admin.AdminQueueMonitoringScreen
import com.example.campusq.ui.admin.AdminAppointmentManagementScreen
import com.example.campusq.ui.admin.AdminReportsAnalyticsScreen
import com.example.campusq.ui.admin.AdminSystemSettingsScreen

@Preview(name = "Phone", widthDp = 393, heightDp = 851)
@Preview(name = "Small phone, large text", widthDp = 320, heightDp = 640, fontScale = 1.5f)
annotation class CampusQScreenPreview

@Composable
fun CampusQPreviewContent(screen: Int) {
    val repo = remember { DemoRepository(java.time.Clock.fixed(java.time.Instant.parse("2026-11-09T00:00:00Z"), java.time.ZoneId.of("Asia/Manila"))) }
    CampusQTheme {
        when (screen) {
            1 -> SplashScreen(onNavigateToLogin = {}, onNavigateToDemoHub = {})
            2 -> LoginScreen(onNavigateToRegister = {}, onNavigateToDemoHub = {}, onOpenStudentDemo = {}, onOpenStaffDemo = {}, onOpenAdminDemo = {})
            3 -> RegistrationScreen(onNavigateBackToLogin = {}, onNavigateToDemoHub = {})
            4 -> StudentHomeDashboardScreen(studentProfile = repo.studentProfile.collectAsState().value, activeTicket = repo.queueTickets.collectAsState().value.first(), notifications = repo.notifications.collectAsState().value, onNavigateBottom = {}, onOpenServices = {}, onOpenBookAppointment = {}, onOpenQueueStatus = {}, onOpenHistory = {}, onMarkNotificationsRead = {}, onRestoreSampleTicket = {}, onSwitchRolePreview = {})
            5 -> RegistrarServicesScreen(services = repo.services.collectAsState().value, onBackClick = {}, onJoinQueueForService = {}, onBookAppointmentForService = {})
            6 -> QueueStatusScreen(activeTicket = repo.queueTickets.collectAsState().value.first(), currentlyServingTicketNumber = "R-005", onNavigateBottom = {}, onCancelTicket = {}, onJoinNewQueue = {}, onRestoreSampleTicket = {})
            7 -> BookAppointmentScreen(services = repo.services.collectAsState().value, preselectedService = null, onNavigateBottom = {}, onConfirmBooking = { _, _, _ -> })
            8 -> TransactionHistoryScreen(transactions = repo.transactions.collectAsState().value, onNavigateBottom = {})
            9 -> StudentProfileScreen(studentProfile = repo.studentProfile.collectAsState().value, onNavigateBottom = {}, onUpdateProfile = { _, _, _ -> }, onLogoutToPreviewHub = {})
            10 -> StaffDashboardScreen(currentDateText = repo.currentFormattedDate(), tickets = repo.queueTickets.collectAsState().value, transactions = repo.transactions.collectAsState().value, onNavigateBottom = {}, onCallNext = { null }, onOpenQueueManagement = {}, onOpenAppointments = {}, onOpenServiceStatus = {}, onOpenTicketDetails = {}, onSwitchRolePreview = {})
            11 -> StaffQueueManagementScreen(tickets = repo.queueTickets.collectAsState().value, onNavigateBottom = {}, onOpenTicketDetails = {}, onCallTicket = {})
            12 -> StaffTicketDetailsScreen(ticket = repo.queueTickets.collectAsState().value.first(), onBackClick = {}, onUpdateStatus = { _, _ -> }, onNavigateToServiceInProgress = {})
            13 -> ServiceInProgressScreen(ticket = repo.queueTickets.collectAsState().value.first { it.status == QueueStatus.SERVING }, onBackClick = {}, onCompleteService = {}, onMarkNoShowOrCancel = {})
            14 -> StaffAppointmentManagementScreen(appointments = repo.appointments.collectAsState().value, onNavigateBottom = {}, onUpdateAppointmentStatus = { _, _ -> })
            15 -> StaffServiceStatusScreen(tickets = repo.queueTickets.collectAsState().value, onNavigateBottom = {}, onOpenTicketDetails = {})
            16 -> StaffProfileScreen(staffProfile = repo.currentStaffProfile.collectAsState().value, onNavigateBottom = {}, onLogoutToPreviewHub = {})
            17 -> AdminDashboardScreen(currentDateText = repo.currentFormattedDate(), services = repo.services.collectAsState().value, staffMembers = repo.staffMembers.collectAsState().value, tickets = repo.queueTickets.collectAsState().value, transactions = repo.transactions.collectAsState().value, onNavigate = {}, onExitToPreviewHub = {})
            18 -> AdminManageServicesScreen(services = repo.services.collectAsState().value, onNavigate = {}, onExitToPreviewHub = {}, onSaveService = { _, _, _, _, _, _, _, _, _ -> }, onToggleServiceAvailability = {})
            19 -> AdminStaffManagementScreen(staffMembers = repo.staffMembers.collectAsState().value, onNavigate = {}, onExitToPreviewHub = {}, onSaveStaff = { _, _, _, _, _ -> }, onToggleStaffActive = {})
            20 -> AdminQueueMonitoringScreen(tickets = repo.queueTickets.collectAsState().value, onNavigate = {}, onExitToPreviewHub = {}, onOpenTicketDetails = {})
            21 -> AdminAppointmentManagementScreen(appointments = repo.appointments.collectAsState().value, onNavigate = {}, onExitToPreviewHub = {})
            22 -> AdminReportsAnalyticsScreen(getAnalyticsForPeriod = { repo.getAnalyticsByService(it) }, onNavigate = {}, onExitToPreviewHub = {})
            23 -> AdminSystemSettingsScreen(settings = repo.institutionSettings.collectAsState().value, onUpdateSettings = {}, onNavigate = {}, onExitToPreviewHub = {})
            24 -> DemoRolePreviewHubScreen(onOpenStudent = {}, onOpenStaff = {}, onOpenAdmin = {}, onNavigateToSplash = {}, onNavigateToLogin = {}, onNavigateToRegister = {})
        }
    }
}

@CampusQScreenPreview
@Composable
fun Preview01SplashScreen() {
    CampusQPreviewContent(1)
}

@CampusQScreenPreview
@Composable
fun Preview02LoginScreen() {
    CampusQPreviewContent(2)
}

@CampusQScreenPreview
@Composable
fun Preview03RegistrationScreen() {
    CampusQPreviewContent(3)
}

@CampusQScreenPreview
@Composable
fun Preview04StudentHomeDashboard() {
    CampusQPreviewContent(4)
}

@CampusQScreenPreview
@Composable
fun Preview05RegistrarServices() {
    CampusQPreviewContent(5)
}

@CampusQScreenPreview
@Composable
fun Preview06QueueStatusScreen() {
    CampusQPreviewContent(6)
}

@CampusQScreenPreview
@Composable
fun Preview07BookAppointmentScreen() {
    CampusQPreviewContent(7)
}

@CampusQScreenPreview
@Composable
fun Preview08TransactionHistory() {
    CampusQPreviewContent(8)
}

@CampusQScreenPreview
@Composable
fun Preview09StudentProfile() {
    CampusQPreviewContent(9)
}

@CampusQScreenPreview
@Composable
fun Preview10StaffDashboardScreen() {
    CampusQPreviewContent(10)
}

@CampusQScreenPreview
@Composable
fun Preview11StaffQueueManagementScreen() {
    CampusQPreviewContent(11)
}

@CampusQScreenPreview
@Composable
fun Preview12StaffTicketDetails() {
    CampusQPreviewContent(12)
}

@CampusQScreenPreview
@Composable
fun Preview13ServiceInProgress() {
    CampusQPreviewContent(13)
}

@CampusQScreenPreview
@Composable
fun Preview14StaffAppointments() {
    CampusQPreviewContent(14)
}

@CampusQScreenPreview
@Composable
fun Preview15StaffServiceStatus() {
    CampusQPreviewContent(15)
}

@CampusQScreenPreview
@Composable
fun Preview16StaffProfile() {
    CampusQPreviewContent(16)
}

@CampusQScreenPreview
@Composable
fun Preview17AdminDashboardScreen() {
    CampusQPreviewContent(17)
}

@CampusQScreenPreview
@Composable
fun Preview18AdminManageServices() {
    CampusQPreviewContent(18)
}

@CampusQScreenPreview
@Composable
fun Preview19AdminStaffManagement() {
    CampusQPreviewContent(19)
}

@CampusQScreenPreview
@Composable
fun Preview20AdminQueueMonitoring() {
    CampusQPreviewContent(20)
}

@CampusQScreenPreview
@Composable
fun Preview21AdminAppointments() {
    CampusQPreviewContent(21)
}

@CampusQScreenPreview
@Composable
fun Preview22AdminReportsAnalyticsScreen() {
    CampusQPreviewContent(22)
}

@CampusQScreenPreview
@Composable
fun Preview23AdminSystemSettingsScreen() {
    CampusQPreviewContent(23)
}

@CampusQScreenPreview
@Composable
fun Preview24DemoRoleHub() {
    CampusQPreviewContent(24)
}
