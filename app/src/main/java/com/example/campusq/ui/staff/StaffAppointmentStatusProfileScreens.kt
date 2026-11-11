package com.example.campusq.ui.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.campusq.data.model.canTransitionTo
import com.example.campusq.ui.theme.CampusQGreenDark
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusq.R
import com.example.campusq.data.model.Appointment
import com.example.campusq.data.model.AppointmentStatus
import com.example.campusq.data.model.QueueStatus
import com.example.campusq.data.model.QueueTicket
import com.example.campusq.data.model.StaffProfile
import com.example.campusq.ui.components.CampusQAppointmentCard
import com.example.campusq.ui.components.CampusQBottomNavigation
import com.example.campusq.ui.components.CampusQCard
import com.example.campusq.ui.components.CampusQConfirmationDialog
import com.example.campusq.ui.components.CampusQEmptyState
import com.example.campusq.ui.components.CampusQFilterTabs
import com.example.campusq.ui.components.CampusQProfileHeader
import com.example.campusq.ui.components.CampusQProfileMenuItem
import com.example.campusq.ui.components.CampusQQueueStatusChip
import com.example.campusq.ui.components.CampusQTopAppBar
import com.example.campusq.ui.navigation.CampusQRoutes
import com.example.campusq.ui.theme.CampusQBackground
import com.example.campusq.ui.theme.CampusQError
import com.example.campusq.ui.theme.CampusQGreenLight
import com.example.campusq.ui.theme.CampusQGreenPrimary
import com.example.campusq.ui.theme.CampusQSurface
import com.example.campusq.ui.theme.CampusQTextPrimary
import com.example.campusq.ui.theme.CampusQTextSecondary

/**
 * SCREEN 14 — STAFF APPOINTMENT MANAGEMENT
 */
@Composable
fun StaffAppointmentManagementScreen(
    appointments: List<Appointment>,
    onNavigateBottom: (String) -> Unit,
    onUpdateAppointmentStatus: (String, AppointmentStatus) -> Unit
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }

    val tabs = listOf("Today", "Upcoming", "All")
    val filteredAppointments = appointments.filter { apt ->
        when (selectedTabIndex) {
            0 -> apt.isToday
            1 -> !apt.isToday
            else -> true
        }
    }

    Scaffold(
        containerColor = CampusQBackground,
        topBar = {
            CampusQTopAppBar(title = "Appointments")
        },
        bottomBar = {
            CampusQBottomNavigation(
                destinations = staffBottomDestinations,
                currentRoute = CampusQRoutes.STAFF_APPOINTMENTS,
                onNavigate = onNavigateBottom
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            CampusQFilterTabs(
                tabs = tabs,
                selectedIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredAppointments.isEmpty()) {
                CampusQEmptyState(
                    title = "No ${tabs[selectedTabIndex]} Appointments",
                    message = "No demonstration appointments match the selected filter.",
                    icon = Icons.Default.CalendarMonth
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredAppointments, key = { it.id }) { apt ->
                        CampusQAppointmentCard(
                            appointment = apt,
                            onClick = { selectedAppointment = apt },
                            actionSlot = {
                                TextButton(onClick = { selectedAppointment = apt }) {
                                    Text(
                                        text = "Manage",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = CampusQGreenDark,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (selectedAppointment != null) {
        val apt = selectedAppointment!!
        AlertDialog(
            onDismissRequest = { selectedAppointment = null },
            title = {
                Text(
                    text = "${apt.referenceNumber} — ${apt.serviceName}",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Student: ${apt.studentName} (${apt.studentId})", style = MaterialTheme.typography.bodyMedium)
                    Text("Schedule: ${apt.dateText} • ${apt.timeSlot}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Current Status: ${apt.status.displayName} (Demo)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select a valid status transition:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Column {
                        listOf(
                            AppointmentStatus.CONFIRMED to "Confirm",
                            AppointmentStatus.CHECKED_IN to "Check In",
                            AppointmentStatus.SERVING to "Start Service",
                            AppointmentStatus.COMPLETED to "Complete",
                            AppointmentStatus.CANCELLED to "Cancel Appointment",
                            AppointmentStatus.NO_SHOW to "No Show"
                        ).filter { (status, _) -> apt.status.canTransitionTo(status) }.forEach { (status, label) ->
                            TextButton(onClick = {
                                onUpdateAppointmentStatus(apt.id, status)
                                selectedAppointment = null
                            }) { Text(label, color = if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW) CampusQError else CampusQGreenPrimary) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAppointment = null }) {
                    Text("Close", color = CampusQTextSecondary)
                }
            },
            containerColor = CampusQSurface
        )
    }
}

/**
 * SCREEN 15 — SERVICE STATUS
 */
@Composable
fun StaffServiceStatusScreen(
    tickets: List<QueueTicket>,
    onNavigateBottom: (String) -> Unit,
    onOpenTicketDetails: (String) -> Unit
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Ongoing", "Completed")

    val filteredTickets = tickets.filter { ticket ->
        if (selectedTabIndex == 0) {
            ticket.status == QueueStatus.SERVING || ticket.status == QueueStatus.CALLED || ticket.status == QueueStatus.WAITING
        } else {
            ticket.status == QueueStatus.COMPLETED || ticket.status == QueueStatus.CANCELLED || ticket.status == QueueStatus.NO_SHOW
        }
    }

    Scaffold(
        containerColor = CampusQBackground,
        topBar = {
            CampusQTopAppBar(title = "Service Status")
        },
        bottomBar = {
            CampusQBottomNavigation(
                destinations = staffBottomDestinations,
                currentRoute = CampusQRoutes.STAFF_SERVICE_STATUS,
                onNavigate = onNavigateBottom
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            CampusQFilterTabs(
                tabs = tabs,
                selectedIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredTickets.isEmpty()) {
                CampusQEmptyState(
                    title = "No ${tabs[selectedTabIndex]} Records",
                    message = "No demonstration service records found for this filter.",
                    icon = Icons.Default.CheckCircle
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredTickets, key = { it.id }) { ticket ->
                        CampusQCard(
                            onClick = { onOpenTicketDetails(ticket.id) },
                            contentPadding = 14.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CampusQGreenLight,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = CampusQGreenPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${ticket.ticketNumber} — ${ticket.serviceName}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${ticket.studentName} • ${ticket.completedTime ?: ticket.startTime ?: ticket.timeJoined}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = CampusQTextSecondary)
                                    )
                                    if (LocalDensity.current.fontScale > 1.3f) CampusQQueueStatusChip(ticket.status)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                if (LocalDensity.current.fontScale <= 1.3f) CampusQQueueStatusChip(status = ticket.status)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * SCREEN 16 — STAFF PROFILE
 */
@Composable
fun StaffProfileScreen(
    staffProfile: StaffProfile,
    onNavigateBottom: (String) -> Unit,
    onLogoutToPreviewHub: () -> Unit
) {
    var activeModal by rememberSaveable { mutableStateOf<String?>(null) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = CampusQBackground,
        topBar = {
            CampusQTopAppBar(title = "Profile")
        },
        bottomBar = {
            CampusQBottomNavigation(
                destinations = staffBottomDestinations,
                currentRoute = CampusQRoutes.STAFF_PROFILE,
                onNavigate = onNavigateBottom
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CampusQProfileHeader(
                fullName = staffProfile.fullName,
                subtitle1 = staffProfile.roleTitle,
                subtitle2 = "${staffProfile.counterName} • ${staffProfile.email}",
                avatarResId = R.drawable.img_avatar_staff
            )

            Spacer(modifier = Modifier.height(4.dp))

            CampusQProfileMenuItem(
                title = "Today's Schedule",
                subtitle = "Mon – Fri • 8:00 AM – 5:00 PM (${staffProfile.counterName})",
                icon = Icons.Default.Schedule,
                onClick = { activeModal = "schedule" }
            )
            CampusQProfileMenuItem(
                title = "Account Settings",
                subtitle = "Counter assignment and staff profile",
                icon = Icons.Default.Settings,
                onClick = { activeModal = "account" }
            )
            CampusQProfileMenuItem(
                title = "Notifications",
                subtitle = "Queue threshold and appointment check-in alerts",
                icon = Icons.Default.Notifications,
                onClick = { activeModal = "notifications" }
            )
            CampusQProfileMenuItem(
                title = "Help & Support",
                subtitle = "Registrar staff operating manual",
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                onClick = { activeModal = "help" }
            )
            CampusQProfileMenuItem(
                title = "Switch Role Preview",
                subtitle = "Switch between Student, Staff, and Admin UI",
                icon = Icons.Default.SwapHoriz,
                onClick = onLogoutToPreviewHub
            )
            CampusQProfileMenuItem(
                title = "Logout",
                subtitle = "Return to UI Preview / Login entry",
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                isDestructive = true,
                onClick = { showLogoutDialog = true }
            )
        }
    }

    if (activeModal != null) {
        val (title, body) = when (activeModal) {
            "schedule" -> "Today's Schedule (Demo)" to "Assigned Counter: ${staffProfile.counterName}\nShift Hours: 8:00 AM – 5:00 PM\nBreak Window: 12:00 PM – 1:00 PM"
            "account" -> "Staff Account Settings (Demo)" to "Name: ${staffProfile.fullName}\nRole: ${staffProfile.roleTitle}\nEmail: ${staffProfile.email}\n\nNote: Staff account credentials are managed by the Registrar Administrator."
            "notifications" -> "Staff Notification Alerts (Demo)" to "• New waiting ticket alert: Enabled\n• Appointment check-in alert: Enabled\n• Long wait threshold (>20 min): Enabled"
            else -> "Staff Help & Support" to "Use Queue Management to call the next waiting student, start service when they arrive at the window, and mark transactions completed."
        }
        AlertDialog(
            onDismissRequest = { activeModal = null },
            title = { Text(title, style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(body, style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary))
            },
            confirmButton = {
                TextButton(onClick = { activeModal = null }) {
                    Text("Close", color = CampusQGreenDark, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CampusQSurface
        )
    }

    if (showLogoutDialog) {
        CampusQConfirmationDialog(
            title = "Exit Staff Demo?",
            message = "Return to the CampusQ UI Preview / Login screen?",
            confirmText = "Exit Demo",
            isDestructive = true,
            onConfirm = {
                showLogoutDialog = false
                onLogoutToPreviewHub()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}
