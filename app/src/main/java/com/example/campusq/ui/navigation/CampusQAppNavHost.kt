package com.example.campusq.ui.navigation

import com.example.campusq.data.model.UserRole
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
fun CampusQWorkflowApp(
    viewModel: CampusQViewModel,
    modifier: Modifier = Modifier,
    startRoute: String = CampusQRoutes.SPLASH,
    onReturnToAuthentication: (() -> Unit)? = null,
    liveRole: UserRole? = null
) {
    val navController = rememberNavController()
    fun openRoute(route: String) {
        if (liveRole != null && route == CampusQRoutes.DEMO_PREVIEW_HUB) {
            onReturnToAuthentication?.invoke()
        } else if (liveRole == null || localRoleCanOpen(liveRole, route)) {
            navController.navigateSingleTopTo(route)
        } else viewModel.showMessage("This route is not available for your account.")
    }
    LaunchedEffect(viewModel) { viewModel.navigationEvents.collect { openRoute(it) } }
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route.orEmpty()
    if (liveRole != null && currentRoute.isNotEmpty() && !localRoleCanOpen(liveRole, currentRoute)) {
        LaunchedEffect(currentRoute) { openRoute(startRoute) }
        return
    }
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
        roleHome?.let { openRoute(it) }
    }
    val snackbarHostState = remember { SnackbarHostState() }

    val studentProfile by viewModel.studentProfile.collectAsState()
    val currentStaffProfile by viewModel.currentStaffProfile.collectAsState()
    val services by viewModel.services.collectAsState()
    val staffMembers by viewModel.staffMembers.collectAsState()
    val queueTickets by viewModel.queueTickets.collectAsState()
    val studentTicketId by viewModel.studentTicketId.collectAsState()
    val nowServing by viewModel.nowServing.collectAsState()
    val reservedSlots by viewModel.reservedSlots.collectAsState()
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
    val currentlyServingTicketNumber = if (liveRole == UserRole.STUDENT) nowServing[activeStudentTicket?.serviceId] ?: "—" else queueTickets.firstOrNull {
        it.status == QueueStatus.SERVING
    }?.ticketNumber ?: if (liveRole == null) "R-000" else "—"

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startRoute
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
                        openRoute(CampusQRoutes.REGISTER)
                    },
                    onNavigateToDemoHub = {
                        openRoute(CampusQRoutes.DEMO_PREVIEW_HUB)
                    },
                    onOpenStudentDemo = {
                        openRoute(CampusQRoutes.STUDENT_HOME)
                    },
                    onOpenStaffDemo = {
                        openRoute(CampusQRoutes.STAFF_DASHBOARD)
                    },
                    onOpenAdminDemo = {
                        openRoute(CampusQRoutes.ADMIN_DASHBOARD)
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
                        openRoute(CampusQRoutes.DEMO_PREVIEW_HUB)
                    }
                )
            }

            // DEMO ROLE PREVIEW HUB
            composable(CampusQRoutes.DEMO_PREVIEW_HUB) {
                DemoRolePreviewHubScreen(
                    onOpenStudent = { openRoute(CampusQRoutes.STUDENT_HOME) },
                    onOpenStaff = { openRoute(CampusQRoutes.STAFF_DASHBOARD) },
                    onOpenAdmin = { openRoute(CampusQRoutes.ADMIN_DASHBOARD) },
                    onNavigateToSplash = { openRoute(CampusQRoutes.SPLASH) },
                    onNavigateToLogin = { onReturnToAuthentication?.invoke() ?: openRoute(CampusQRoutes.LOGIN) },
                    onNavigateToRegister = { openRoute(CampusQRoutes.REGISTER) }
                )
            }

            // SCREEN 04 — STUDENT HOME DASHBOARD
            composable(CampusQRoutes.STUDENT_HOME) {
                StudentHomeDashboardScreen(
                    studentProfile = studentProfile,
                    activeTicket = activeStudentTicket,
                    notifications = notifications,
                    onNavigateBottom = { route -> openRoute(route) },
                    onOpenServices = { openRoute(CampusQRoutes.STUDENT_SERVICES) },
                    onOpenBookAppointment = {
                        viewModel.selectServiceForBooking(null)
                        openRoute(CampusQRoutes.STUDENT_BOOK_APPOINTMENT)
                    },
                    onOpenQueueStatus = { openRoute(CampusQRoutes.STUDENT_QUEUE_STATUS) },
                    onOpenHistory = { openRoute(CampusQRoutes.STUDENT_HISTORY) },
                    onMarkNotificationsRead = { viewModel.markAllNotificationsRead() },
                    onRestoreSampleTicket = { viewModel.restoreDemoTicketR001() },
                    onSwitchRolePreview = { openRoute(CampusQRoutes.DEMO_PREVIEW_HUB) }
                )
            }

            // SCREEN 05 — REGISTRAR SERVICES
            composable(CampusQRoutes.STUDENT_SERVICES) {
                RegistrarServicesScreen(
                    services = services,
                    onBackClick = { navController.popBackStack() },
                    onJoinQueueForService = { service ->
                        if (viewModel.joinQueue(service) != null) {
                            openRoute(CampusQRoutes.STUDENT_QUEUE_STATUS)
                        }
                    },
                    onBookAppointmentForService = { service ->
                        viewModel.selectServiceForBooking(service)
                        openRoute(CampusQRoutes.STUDENT_BOOK_APPOINTMENT)
                    }
                )
            }

            // SCREEN 06 — QUEUE STATUS
            composable(CampusQRoutes.STUDENT_QUEUE_STATUS) {
                QueueStatusScreen(
                    activeTicket = activeStudentTicket,
                    currentlyServingTicketNumber = currentlyServingTicketNumber,
                    onNavigateBottom = { route -> openRoute(route) },
                    onCancelTicket = { ticketId -> viewModel.cancelStudentTicket(ticketId) },
                    onJoinNewQueue = { openRoute(CampusQRoutes.STUDENT_SERVICES) },
                    onRestoreSampleTicket = { viewModel.restoreDemoTicketR001() }
                )
            }

            // SCREEN 07 — BOOK APPOINTMENT
            composable(CampusQRoutes.STUDENT_BOOK_APPOINTMENT) {
                BookAppointmentScreen(
                    services = services,
                    preselectedService = selectedServiceForBooking,
                    appointments = appointments.filter { it.studentId == studentProfile.studentId },
                    reservedSlots = reservedSlots,
                    onCancelAppointment = viewModel::cancelAppointment,
                    onRescheduleAppointment = viewModel::rescheduleAppointment,
                    onNavigateBottom = { route -> openRoute(route) },
                    onConfirmBooking = { service, dateText, timeSlot ->
                        if (viewModel.bookAppointment(service, dateText, timeSlot) != null) {
                            openRoute(CampusQRoutes.STUDENT_HISTORY)
                        }
                    }
                )
            }

            // SCREEN 08 — TRANSACTION HISTORY
            composable(CampusQRoutes.STUDENT_HISTORY) {
                TransactionHistoryScreen(
                    transactions = transactions.filter { it.ownerId == studentProfile.id },
                    onNavigateBottom = { route -> openRoute(route) }
                )
            }

            // SCREEN 09 — STUDENT PROFILE
            composable(CampusQRoutes.STUDENT_PROFILE) {
                StudentProfileScreen(
                    studentProfile = studentProfile,
                    onNavigateBottom = { route -> openRoute(route) },
                    onUpdateProfile = { name, id, email ->
                        viewModel.updateStudentProfile(name, id, email)
                    },
                    onLogoutToPreviewHub = {
                        openRoute(CampusQRoutes.DEMO_PREVIEW_HUB)
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
                    onNavigateBottom = { route -> openRoute(route) },
                    onCallNext = { viewModel.callNextWaitingTicket() },
                    onOpenQueueManagement = { openRoute(CampusQRoutes.STAFF_QUEUE_MANAGEMENT) },
                    onOpenAppointments = { openRoute(CampusQRoutes.STAFF_APPOINTMENTS) },
                    onOpenServiceStatus = { openRoute(CampusQRoutes.STAFF_SERVICE_STATUS) },
                    onOpenTicketDetails = { ticketId ->
                        openRoute(CampusQRoutes.staffTicketDetailsRoute(ticketId))
                    },
                    onSwitchRolePreview = { openRoute(CampusQRoutes.DEMO_PREVIEW_HUB) }
                )
            }

            // SCREEN 11 — QUEUE MANAGEMENT
            composable(CampusQRoutes.STAFF_QUEUE_MANAGEMENT) {
                StaffQueueManagementScreen(
                    tickets = queueTickets,
                    onNavigateBottom = { route -> openRoute(route) },
                    onOpenTicketDetails = { ticketId ->
                        openRoute(CampusQRoutes.staffTicketDetailsRoute(ticketId))
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
                        if (liveRole == null || queueTickets.any { it.id == id && it.status == QueueStatus.SERVING }) openRoute(CampusQRoutes.staffServiceInProgressRoute(id))
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
                        val destination = if (liveRole == UserRole.ADMIN) CampusQRoutes.ADMIN_QUEUE_MONITORING else CampusQRoutes.STAFF_SERVICE_STATUS
                        viewModel.updateTicketStatus(id, QueueStatus.COMPLETED, if (liveRole != null) destination else null)
                        if (liveRole == null) openRoute(destination)
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
                    onNavigateBottom = { route -> openRoute(route) },
                    onUpdateAppointmentStatus = { id, status ->
                        viewModel.updateAppointmentStatus(id, status)
                    }
                )
            }

            // SCREEN 15 — SERVICE STATUS
            composable(CampusQRoutes.STAFF_SERVICE_STATUS) {
                StaffServiceStatusScreen(
                    tickets = queueTickets,
                    onNavigateBottom = { route -> openRoute(route) },
                    onOpenTicketDetails = { ticketId ->
                        openRoute(CampusQRoutes.staffTicketDetailsRoute(ticketId))
                    }
                )
            }

            // SCREEN 16 — STAFF PROFILE
            composable(CampusQRoutes.STAFF_PROFILE) {
                StaffProfileScreen(
                    staffProfile = currentStaffProfile,
                    onNavigateBottom = { route -> openRoute(route) },
                    onLogoutToPreviewHub = {
                        openRoute(CampusQRoutes.DEMO_PREVIEW_HUB)
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
                    onNavigate = { route -> openRoute(route) },
                    onExitToPreviewHub = { openRoute(CampusQRoutes.DEMO_PREVIEW_HUB) }
                )
            }

            // SCREEN 18 — MANAGE SERVICES
            composable(CampusQRoutes.ADMIN_MANAGE_SERVICES) {
                AdminManageServicesScreen(
                    services = services,
                    onNavigate = { route -> openRoute(route) },
                    onExitToPreviewHub = { openRoute(CampusQRoutes.DEMO_PREVIEW_HUB) },
                    onSaveService = { id, name, desc, reqs, icon, avail, queue, apt, mins ->
                        viewModel.saveService(id, name, desc, reqs, icon, avail, queue, apt, mins)
                    },
                    onToggleServiceAvailability = { id -> viewModel.toggleServiceAvailability(id) },
                    onDeleteService = viewModel::deleteUnusedService
                )
            }

            // SCREEN 19 — STAFF MANAGEMENT
            composable(CampusQRoutes.ADMIN_STAFF_MANAGEMENT) {
                AdminStaffManagementScreen(
                    staffMembers = staffMembers,
                    onNavigate = { route -> openRoute(route) },
                    onExitToPreviewHub = { openRoute(CampusQRoutes.DEMO_PREVIEW_HUB) },
                    onSaveStaff = { id, name, email, counter, active ->
                        viewModel.saveStaffMember(id, name, email, counter, active)
                    },
                    onToggleStaffActive = { id -> viewModel.toggleStaffActive(id) },
                    onCreateStaff = { name, email, counter, active, password -> viewModel.saveStaffMember(null, name, email, counter, active, password) }
                )
            }

            // SCREEN 20 — QUEUE MONITORING
            composable(CampusQRoutes.ADMIN_QUEUE_MONITORING) {
                AdminQueueMonitoringScreen(
                    tickets = queueTickets,
                    onNavigate = { route -> openRoute(route) },
                    onExitToPreviewHub = { openRoute(CampusQRoutes.DEMO_PREVIEW_HUB) },
                    onOpenTicketDetails = { ticketId ->
                        openRoute(CampusQRoutes.staffTicketDetailsRoute(ticketId))
                    }
                )
            }

            // SCREEN 21 — ADMIN APPOINTMENT MANAGEMENT
            composable(CampusQRoutes.ADMIN_APPOINTMENTS) {
                AdminAppointmentManagementScreen(
                    appointments = appointments,
                    onNavigate = { route -> openRoute(route) },
                    onExitToPreviewHub = { openRoute(CampusQRoutes.DEMO_PREVIEW_HUB) }
                )
            }

            // SCREEN 22 — REPORTS AND ANALYTICS
            composable(CampusQRoutes.ADMIN_REPORTS) {
                AdminReportsAnalyticsScreen(
                    getAnalyticsForPeriod = { periodIdx -> viewModel.getAnalyticsByService(periodIdx) },
                    onNavigate = { route -> openRoute(route) },
                    onExitToPreviewHub = { openRoute(CampusQRoutes.DEMO_PREVIEW_HUB) }
                )
            }

            // SCREEN 23 — SYSTEM SETTINGS
            composable(CampusQRoutes.ADMIN_SYSTEM_SETTINGS) {
                AdminSystemSettingsScreen(
                    settings = institutionSettings,
                    onUpdateSettings = { updated -> viewModel.updateInstitutionSettings(updated) },
                    onNavigate = { route -> openRoute(route) },
                    onExitToPreviewHub = { openRoute(CampusQRoutes.DEMO_PREVIEW_HUB) }
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

/** Historical preview host. MainActivity uses the authenticated local-server root. */
@Composable
fun CampusQDemoApp(
    viewModel: CampusQViewModel,
    modifier: Modifier = Modifier,
    startRoute: String = CampusQRoutes.SPLASH,
    onReturnToAuthentication: (() -> Unit)? = null
) {
    CampusQWorkflowApp(viewModel, modifier, startRoute, onReturnToAuthentication)
}
