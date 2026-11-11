package com.example.campusq.ui.navigation

import android.app.Activity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.activity.compose.BackHandler
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.campusq.data.model.isActive
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.campusq.data.model.QueueStatus
import com.example.campusq.ui.admin.AdminAppointmentManagementScreen
import com.example.campusq.ui.admin.AdminDashboardScreen
import com.example.campusq.ui.admin.AdminManageServicesScreen
import com.example.campusq.ui.admin.AdminQueueMonitoringScreen
import com.example.campusq.ui.admin.AdminReportsAnalyticsScreen
import com.example.campusq.ui.admin.AdminStaffManagementScreen
import com.example.campusq.ui.admin.AdminSystemSettingsScreen
import com.example.campusq.ui.auth.DemoRolePreviewHubScreen
import com.example.campusq.ui.auth.LoginScreen
import com.example.campusq.ui.auth.RegistrationScreen
import com.example.campusq.ui.auth.SplashScreen
import com.example.campusq.ui.staff.ServiceInProgressScreen
import com.example.campusq.ui.staff.StaffAppointmentManagementScreen
import com.example.campusq.ui.staff.StaffDashboardScreen
import com.example.campusq.ui.staff.StaffProfileScreen
import com.example.campusq.ui.staff.StaffQueueManagementScreen
import com.example.campusq.ui.staff.StaffServiceStatusScreen
import com.example.campusq.ui.staff.StaffTicketDetailsScreen
import com.example.campusq.ui.student.BookAppointmentScreen
import com.example.campusq.ui.student.QueueStatusScreen
import com.example.campusq.ui.student.RegistrarServicesScreen
import com.example.campusq.ui.student.StudentHomeDashboardScreen
import com.example.campusq.ui.student.StudentProfileScreen
import com.example.campusq.ui.student.TransactionHistoryScreen
import com.example.campusq.viewmodel.CampusQViewModel

private fun NavHostController.navigateSingleTopTo(route: String) {
    val home = CampusQRoutes.roleHome(route)
    navigate(route) {
        launchSingleTop = true
        if (route == CampusQRoutes.DEMO_PREVIEW_HUB) {
            popUpTo(graph.id) { inclusive = false }
        } else if (route in CampusQRoutes.roleTabs && home != null) {
            if (runCatching { getBackStackEntry(home) }.isSuccess) {
                popUpTo(home)
                restoreState = false
            } else {
                popUpTo(CampusQRoutes.DEMO_PREVIEW_HUB) { inclusive = false }
            }
        }
    }
}

@Composable
fun CampusQApp(
    viewModel: CampusQViewModel = viewModel()
) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route.orEmpty()
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = CampusQRoutes.roleHome(currentRoute) == null
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }
    val roleHome = CampusQRoutes.roleHome(currentRoute)
    BackHandler(enabled = currentRoute in CampusQRoutes.roleTabs && currentRoute != roleHome) {
        roleHome?.let { navController.navigateSingleTopTo(it) }
    }
    val snackbarHostState = remember { SnackbarHostState() }

    val studentProfile by viewModel.studentProfile.collectAsState()
    val currentStaffProfile by viewModel.currentStaffProfile.collectAsState()
    val services by viewModel.services.collectAsState()
    val staffMembers by viewModel.staffMembers.collectAsState()
    val queueTickets by viewModel.queueTickets.collectAsState()
    val studentTicketId by viewModel.studentTicketId.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val institutionSettings by viewModel.institutionSettings.collectAsState()
    val selectedServiceForBooking by viewModel.selectedServiceForBooking.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    val activeStudentTicket = queueTickets.find { it.id == studentTicketId && it.status.isActive }
    val currentlyServingTicketNumber = queueTickets.firstOrNull {
        it.status == QueueStatus.SERVING
    }?.ticketNumber ?: "R-000"

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = CampusQRoutes.SPLASH
        ) {
            // SCREEN 01 — SPLASH SCREEN
            composable(CampusQRoutes.SPLASH) {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(CampusQRoutes.LOGIN) {
                            popUpTo(CampusQRoutes.SPLASH) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToDemoHub = {
                        navController.navigate(CampusQRoutes.DEMO_PREVIEW_HUB) {
                            popUpTo(CampusQRoutes.SPLASH) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // SCREEN 02 — LOGIN SCREEN
            composable(CampusQRoutes.LOGIN) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigateSingleTopTo(CampusQRoutes.REGISTER)
                    },
                    onNavigateToDemoHub = {
                        navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB)
                    },
                    onOpenStudentDemo = {
                        navController.navigateSingleTopTo(CampusQRoutes.STUDENT_HOME)
                    },
                    onOpenStaffDemo = {
                        navController.navigateSingleTopTo(CampusQRoutes.STAFF_DASHBOARD)
                    },
                    onOpenAdminDemo = {
                        navController.navigateSingleTopTo(CampusQRoutes.ADMIN_DASHBOARD)
                    }
                )
            }

            // SCREEN 03 — CREATE ACCOUNT / REGISTRATION
            composable(CampusQRoutes.REGISTER) {
                RegistrationScreen(
                    onNavigateBackToLogin = {
                        navController.popBackStack()
                    },
                    onNavigateToDemoHub = {
                        navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB)
                    }
                )
            }

            // DEMO ROLE PREVIEW HUB
            composable(CampusQRoutes.DEMO_PREVIEW_HUB) {
                DemoRolePreviewHubScreen(
                    onOpenStudent = { navController.navigateSingleTopTo(CampusQRoutes.STUDENT_HOME) },
                    onOpenStaff = { navController.navigateSingleTopTo(CampusQRoutes.STAFF_DASHBOARD) },
                    onOpenAdmin = { navController.navigateSingleTopTo(CampusQRoutes.ADMIN_DASHBOARD) },
                    onNavigateToSplash = { navController.navigateSingleTopTo(CampusQRoutes.SPLASH) },
                    onNavigateToLogin = { navController.navigateSingleTopTo(CampusQRoutes.LOGIN) },
                    onNavigateToRegister = { navController.navigateSingleTopTo(CampusQRoutes.REGISTER) }
                )
            }

            // SCREEN 04 — STUDENT HOME DASHBOARD
            composable(CampusQRoutes.STUDENT_HOME) {
                StudentHomeDashboardScreen(
                    studentProfile = studentProfile,
                    activeTicket = activeStudentTicket,
                    notifications = notifications,
                    onNavigateBottom = { route -> navController.navigateSingleTopTo(route) },
                    onOpenServices = { navController.navigateSingleTopTo(CampusQRoutes.STUDENT_SERVICES) },
                    onOpenBookAppointment = {
                        viewModel.selectServiceForBooking(null)
                        navController.navigateSingleTopTo(CampusQRoutes.STUDENT_BOOK_APPOINTMENT)
                    },
                    onOpenQueueStatus = { navController.navigateSingleTopTo(CampusQRoutes.STUDENT_QUEUE_STATUS) },
                    onOpenHistory = { navController.navigateSingleTopTo(CampusQRoutes.STUDENT_HISTORY) },
                    onMarkNotificationsRead = { viewModel.markAllNotificationsRead() },
                    onRestoreSampleTicket = { viewModel.restoreDemoTicketR001() },
                    onSwitchRolePreview = { navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB) }
                )
            }

            // SCREEN 05 — REGISTRAR SERVICES
            composable(CampusQRoutes.STUDENT_SERVICES) {
                RegistrarServicesScreen(
                    services = services,
                    onBackClick = { navController.popBackStack() },
                    onJoinQueueForService = { service ->
                        if (viewModel.joinQueue(service) != null) {
                            navController.navigateSingleTopTo(CampusQRoutes.STUDENT_QUEUE_STATUS)
                        }
                    },
                    onBookAppointmentForService = { service ->
                        viewModel.selectServiceForBooking(service)
                        navController.navigateSingleTopTo(CampusQRoutes.STUDENT_BOOK_APPOINTMENT)
                    }
                )
            }

            // SCREEN 06 — QUEUE STATUS
            composable(CampusQRoutes.STUDENT_QUEUE_STATUS) {
                QueueStatusScreen(
                    activeTicket = activeStudentTicket,
                    currentlyServingTicketNumber = currentlyServingTicketNumber,
                    onNavigateBottom = { route -> navController.navigateSingleTopTo(route) },
                    onCancelTicket = { ticketId -> viewModel.cancelStudentTicket(ticketId) },
                    onJoinNewQueue = { navController.navigateSingleTopTo(CampusQRoutes.STUDENT_SERVICES) },
                    onRestoreSampleTicket = { viewModel.restoreDemoTicketR001() }
                )
            }

            // SCREEN 07 — BOOK APPOINTMENT
            composable(CampusQRoutes.STUDENT_BOOK_APPOINTMENT) {
                BookAppointmentScreen(
                    services = services,
                    preselectedService = selectedServiceForBooking,
                    appointments = appointments.filter { it.studentId == studentProfile.studentId },
                    onCancelAppointment = viewModel::cancelAppointment,
                    onRescheduleAppointment = viewModel::rescheduleAppointment,
                    onNavigateBottom = { route -> navController.navigateSingleTopTo(route) },
                    onConfirmBooking = { service, dateText, timeSlot ->
                        if (viewModel.bookAppointment(service, dateText, timeSlot) != null) {
                            navController.navigateSingleTopTo(CampusQRoutes.STUDENT_HISTORY)
                        }
                    }
                )
            }

            // SCREEN 08 — TRANSACTION HISTORY
            composable(CampusQRoutes.STUDENT_HISTORY) {
                TransactionHistoryScreen(
                    transactions = transactions.filter { it.ownerId == studentProfile.id },
                    onNavigateBottom = { route -> navController.navigateSingleTopTo(route) }
                )
            }

            // SCREEN 09 — STUDENT PROFILE
            composable(CampusQRoutes.STUDENT_PROFILE) {
                StudentProfileScreen(
                    studentProfile = studentProfile,
                    onNavigateBottom = { route -> navController.navigateSingleTopTo(route) },
                    onUpdateProfile = { name, id, email ->
                        viewModel.updateStudentProfile(name, id, email)
                    },
                    onLogoutToPreviewHub = {
                        navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB)
                    }
                )
            }

            // SCREEN 10 — STAFF DASHBOARD
            composable(CampusQRoutes.STAFF_DASHBOARD) {
                StaffDashboardScreen(
                    currentDateText = viewModel.currentFormattedDate(),
                    counterName = currentStaffProfile.counterName,
                    tickets = queueTickets,
                    transactions = transactions,
                    onNavigateBottom = { route -> navController.navigateSingleTopTo(route) },
                    onCallNext = { viewModel.callNextWaitingTicket() },
                    onOpenQueueManagement = { navController.navigateSingleTopTo(CampusQRoutes.STAFF_QUEUE_MANAGEMENT) },
                    onOpenAppointments = { navController.navigateSingleTopTo(CampusQRoutes.STAFF_APPOINTMENTS) },
                    onOpenServiceStatus = { navController.navigateSingleTopTo(CampusQRoutes.STAFF_SERVICE_STATUS) },
                    onOpenTicketDetails = { ticketId ->
                        navController.navigateSingleTopTo(CampusQRoutes.staffTicketDetailsRoute(ticketId))
                    },
                    onSwitchRolePreview = { navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB) }
                )
            }

            // SCREEN 11 — QUEUE MANAGEMENT
            composable(CampusQRoutes.STAFF_QUEUE_MANAGEMENT) {
                StaffQueueManagementScreen(
                    tickets = queueTickets,
                    onNavigateBottom = { route -> navController.navigateSingleTopTo(route) },
                    onOpenTicketDetails = { ticketId ->
                        navController.navigateSingleTopTo(CampusQRoutes.staffTicketDetailsRoute(ticketId))
                    },
                    onCallTicket = { ticket ->
                        viewModel.updateTicketStatus(ticket.id, QueueStatus.CALLED)
                    }
                )
            }

            // SCREEN 12 — STAFF TICKET DETAILS / ACTIONS
            composable(
                route = CampusQRoutes.STAFF_TICKET_DETAILS,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId")
                val ticket = queueTickets.find { it.id == ticketId }
                StaffTicketDetailsScreen(
                    ticket = ticket,
                    onBackClick = { navController.popBackStack() },
                    onUpdateStatus = { id, newStatus ->
                        viewModel.updateTicketStatus(id, newStatus)
                    },
                    onNavigateToServiceInProgress = { id ->
                        navController.navigateSingleTopTo(CampusQRoutes.staffServiceInProgressRoute(id))
                    }
                )
            }

            // SCREEN 13 — SERVICE IN PROGRESS
            composable(
                route = CampusQRoutes.STAFF_SERVICE_IN_PROGRESS,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId")
                val ticket = queueTickets.find { it.id == ticketId }
                ServiceInProgressScreen(
                    ticket = ticket,
                    counterName = "${currentStaffProfile.counterName} (Registrar)",
                    onBackClick = { navController.popBackStack() },
                    onCompleteService = { id ->
                        viewModel.updateTicketStatus(id, QueueStatus.COMPLETED)
                        navController.navigateSingleTopTo(CampusQRoutes.STAFF_SERVICE_STATUS)
                    },
                    onMarkNoShowOrCancel = { id ->
                        viewModel.updateTicketStatus(id, QueueStatus.NO_SHOW)
                        navController.popBackStack()
                    }
                )
            }

            // SCREEN 14 — STAFF APPOINTMENT MANAGEMENT
            composable(CampusQRoutes.STAFF_APPOINTMENTS) {
                StaffAppointmentManagementScreen(
                    appointments = appointments,
                    onNavigateBottom = { route -> navController.navigateSingleTopTo(route) },
                    onUpdateAppointmentStatus = { id, status ->
                        viewModel.updateAppointmentStatus(id, status)
                    }
                )
            }

            // SCREEN 15 — SERVICE STATUS
            composable(CampusQRoutes.STAFF_SERVICE_STATUS) {
                StaffServiceStatusScreen(
                    tickets = queueTickets,
                    onNavigateBottom = { route -> navController.navigateSingleTopTo(route) },
                    onOpenTicketDetails = { ticketId ->
                        navController.navigateSingleTopTo(CampusQRoutes.staffTicketDetailsRoute(ticketId))
                    }
                )
            }

            // SCREEN 16 — STAFF PROFILE
            composable(CampusQRoutes.STAFF_PROFILE) {
                StaffProfileScreen(
                    staffProfile = currentStaffProfile,
                    onNavigateBottom = { route -> navController.navigateSingleTopTo(route) },
                    onLogoutToPreviewHub = {
                        navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB)
                    }
                )
            }

            // SCREEN 17 — ADMIN DASHBOARD
            composable(CampusQRoutes.ADMIN_DASHBOARD) {
                AdminDashboardScreen(
                    currentDateText = viewModel.currentFormattedDate(),
                    services = services,
                    staffMembers = staffMembers,
                    tickets = queueTickets,
                    transactions = transactions,
                    onNavigate = { route -> navController.navigateSingleTopTo(route) },
                    onExitToPreviewHub = { navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB) }
                )
            }

            // SCREEN 18 — MANAGE SERVICES
            composable(CampusQRoutes.ADMIN_MANAGE_SERVICES) {
                AdminManageServicesScreen(
                    services = services,
                    onNavigate = { route -> navController.navigateSingleTopTo(route) },
                    onExitToPreviewHub = { navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB) },
                    onSaveService = { id, name, desc, reqs, icon, avail, queue, apt, mins ->
                        viewModel.saveService(id, name, desc, reqs, icon, avail, queue, apt, mins)
                    },
                    onToggleServiceAvailability = { id -> viewModel.toggleServiceAvailability(id) }
                )
            }

            // SCREEN 19 — STAFF MANAGEMENT
            composable(CampusQRoutes.ADMIN_STAFF_MANAGEMENT) {
                AdminStaffManagementScreen(
                    staffMembers = staffMembers,
                    onNavigate = { route -> navController.navigateSingleTopTo(route) },
                    onExitToPreviewHub = { navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB) },
                    onSaveStaff = { id, name, email, counter, active ->
                        viewModel.saveStaffMember(id, name, email, counter, active)
                    },
                    onToggleStaffActive = { id -> viewModel.toggleStaffActive(id) }
                )
            }

            // SCREEN 20 — QUEUE MONITORING
            composable(CampusQRoutes.ADMIN_QUEUE_MONITORING) {
                AdminQueueMonitoringScreen(
                    tickets = queueTickets,
                    onNavigate = { route -> navController.navigateSingleTopTo(route) },
                    onExitToPreviewHub = { navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB) },
                    onOpenTicketDetails = { ticketId ->
                        navController.navigateSingleTopTo(CampusQRoutes.staffTicketDetailsRoute(ticketId))
                    }
                )
            }

            // SCREEN 21 — ADMIN APPOINTMENT MANAGEMENT
            composable(CampusQRoutes.ADMIN_APPOINTMENTS) {
                AdminAppointmentManagementScreen(
                    appointments = appointments,
                    onNavigate = { route -> navController.navigateSingleTopTo(route) },
                    onExitToPreviewHub = { navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB) }
                )
            }

            // SCREEN 22 — REPORTS AND ANALYTICS
            composable(CampusQRoutes.ADMIN_REPORTS) {
                AdminReportsAnalyticsScreen(
                    getAnalyticsForPeriod = { periodIdx -> viewModel.getAnalyticsByService(periodIdx) },
                    onNavigate = { route -> navController.navigateSingleTopTo(route) },
                    onExitToPreviewHub = { navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB) }
                )
            }

            // SCREEN 23 — SYSTEM SETTINGS
            composable(CampusQRoutes.ADMIN_SYSTEM_SETTINGS) {
                AdminSystemSettingsScreen(
                    settings = institutionSettings,
                    onUpdateSettings = { updated -> viewModel.updateInstitutionSettings(updated) },
                    onNavigate = { route -> navController.navigateSingleTopTo(route) },
                    onExitToPreviewHub = { navController.navigateSingleTopTo(CampusQRoutes.DEMO_PREVIEW_HUB) }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 84.dp)
        )
    }
}
