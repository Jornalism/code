package com.example.campusq.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.campusq.ui.theme.CampusQError
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusq.data.model.Appointment
import com.example.campusq.data.model.InstitutionSettings
import com.example.campusq.data.model.QueueStatus
import com.example.campusq.data.model.QueueTicket
import com.example.campusq.data.model.ServiceAnalyticsBar
import com.example.campusq.ui.components.CampusQAppointmentCard
import com.example.campusq.ui.components.CampusQCard
import com.example.campusq.ui.components.CampusQEmptyState
import com.example.campusq.ui.components.CampusQFilterTabs
import com.example.campusq.ui.components.CampusQProfileMenuItem
import com.example.campusq.ui.components.CampusQQueueStatusChip
import com.example.campusq.ui.components.CampusQSectionHeading
import com.example.campusq.ui.components.CampusQTextField
import com.example.campusq.ui.navigation.CampusQRoutes
import com.example.campusq.ui.theme.CampusQAdminPurple
import com.example.campusq.ui.theme.CampusQGreenDark
import com.example.campusq.ui.theme.CampusQGreenLight
import com.example.campusq.ui.theme.CampusQGreenPrimary
import com.example.campusq.ui.theme.CampusQInfo
import com.example.campusq.ui.theme.CampusQOutline
import com.example.campusq.ui.theme.CampusQSuccess
import com.example.campusq.ui.theme.CampusQSurface
import com.example.campusq.ui.theme.CampusQSurfaceVariant
import com.example.campusq.ui.theme.CampusQTextPrimary
import com.example.campusq.ui.theme.CampusQTextSecondary

/**
 * SCREEN 20 — QUEUE MONITORING (Green Header per Reference)
 */
@Composable
fun AdminQueueMonitoringScreen(
    tickets: List<QueueTicket>,
    onNavigate: (String) -> Unit,
    onExitToPreviewHub: () -> Unit,
    onOpenTicketDetails: (String) -> Unit
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("All", "Waiting", "Called", "Serving", "Completed")

    val filteredTickets = tickets.filter { ticket ->
        when (selectedTabIndex) {
            1 -> ticket.status == QueueStatus.WAITING
            2 -> ticket.status == QueueStatus.CALLED
            3 -> ticket.status == QueueStatus.SERVING
            4 -> ticket.status == QueueStatus.COMPLETED
            else -> true
        }
    }

    AdminScaffoldWithDrawer(
        title = "Queue Monitoring",
        currentRoute = CampusQRoutes.ADMIN_QUEUE_MONITORING,
        headerColor = CampusQGreenPrimary,
        onNavigate = onNavigate,
        onExitToPreviewHub = onExitToPreviewHub
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shared Local Queue State",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(color = CampusQTextSecondary)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CampusQGreenLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(CampusQGreenPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Demo Sync Active",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CampusQGreenDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            CampusQFilterTabs(
                tabs = tabs,
                selectedIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredTickets.isEmpty()) {
                CampusQEmptyState(
                    title = "No Tickets Found",
                    message = "No demonstration queue records match the ${tabs[selectedTabIndex]} filter."
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
                            if (LocalDensity.current.fontScale > 1.3f) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(ticket.ticketNumber, style = MaterialTheme.typography.titleMedium)
                                    CampusQQueueStatusChip(ticket.status)
                                }
                                Text(ticket.serviceName, style = MaterialTheme.typography.titleMedium)
                                Text("${ticket.studentName} • Wait: ${ticket.waitingDurationText}", style = MaterialTheme.typography.bodySmall)
                            } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = CampusQGreenLight,
                                        modifier = Modifier.padding(end = 12.dp)
                                    ) {
                                        Text(
                                            text = ticket.ticketNumber,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = CampusQGreenDark,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ticket.serviceName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${ticket.studentName} • Wait: ${ticket.waitingDurationText}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                CampusQQueueStatusChip(status = ticket.status)
                            }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * SCREEN 21 — ADMIN APPOINTMENT MANAGEMENT (Green Header per Reference)
 */
@Composable
fun AdminAppointmentManagementScreen(
    appointments: List<Appointment>,
    onNavigate: (String) -> Unit,
    onExitToPreviewHub: () -> Unit
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

    AdminScaffoldWithDrawer(
        title = "Appointments",
        currentRoute = CampusQRoutes.ADMIN_APPOINTMENTS,
        headerColor = CampusQGreenPrimary,
        onNavigate = onNavigate,
        onExitToPreviewHub = onExitToPreviewHub
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
                    title = "No Appointments",
                    message = "No demonstration appointments match the ${tabs[selectedTabIndex]} filter.",
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
                            onClick = { selectedAppointment = apt }
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
            title = { Text("${apt.referenceNumber} — Appointment Details", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Service: ${apt.serviceName}", style = MaterialTheme.typography.bodyMedium)
                    Text("Student: ${apt.studentName} (${apt.studentId})", style = MaterialTheme.typography.bodyMedium)
                    Text("Date & Time: ${apt.dateText} • ${apt.timeSlot}", style = MaterialTheme.typography.bodyMedium)
                    Text("Status: ${apt.status.displayName} (Demo)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedAppointment = null
                        onNavigate(CampusQRoutes.ADMIN_SYSTEM_SETTINGS)
                    }
                ) {
                    Text("Schedule Settings", color = CampusQGreenDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAppointment = null }) {
                    Text("Close", color = CampusQTextSecondary)
                }
            },
            containerColor = CampusQSurface
        )
    }
}

/**
 * SCREEN 22 — REPORTS AND ANALYTICS (Green Header per Reference)
 */
@Composable
fun AdminReportsAnalyticsScreen(
    getAnalyticsForPeriod: (Int) -> List<ServiceAnalyticsBar>,
    onNavigate: (String) -> Unit,
    onExitToPreviewHub: () -> Unit
) {
    val periods = listOf(
        "This Week (Demo)",
        "This Month (Demo)",
        "Last Six Months (Demo)"
    )
    var selectedPeriodIndex by rememberSaveable { mutableIntStateOf(0) }
    var showPeriodDropdown by remember { mutableStateOf(false) }

    val bars = getAnalyticsForPeriod(selectedPeriodIndex)
    val totalTransactions = bars.sumOf { it.count }
    val maxCount = (bars.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)

    AdminScaffoldWithDrawer(
        title = "Reports",
        currentRoute = CampusQRoutes.ADMIN_REPORTS,
        headerColor = CampusQGreenPrimary,
        onNavigate = onNavigate,
        onExitToPreviewHub = onExitToPreviewHub
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Date Range Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                CampusQCard(
                    onClick = { showPeriodDropdown = true },
                    contentPadding = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = CampusQGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Reporting Period (Demo Fixture)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = CampusQTextSecondary)
                                )
                                Text(
                                    text = periods[selectedPeriodIndex],
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Select Reporting Period",
                            tint = CampusQGreenPrimary
                        )
                    }
                }

                DropdownMenu(
                    expanded = showPeriodDropdown,
                    onDismissRequest = { showPeriodDropdown = false },
                    modifier = Modifier.background(CampusQSurface)
                ) {
                    periods.forEachIndexed { idx, label ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedPeriodIndex = idx
                                showPeriodDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Metric Card: Total Transactions
            CampusQCard(
                cornerRadius = 20.dp,
                contentPadding = 18.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total Transactions",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = totalTransactions.toString(),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 38.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CampusQGreenPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sample demonstration analytics • Not live Registrar data",
                            style = MaterialTheme.typography.labelSmall.copy(color = CampusQTextSecondary)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (LocalDensity.current.fontScale <= 1.3f) Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CampusQGreenLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = CampusQSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Demo Trend",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CampusQGreenDark,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Transactions by Service Vertical Bar Chart
            CampusQCard(
                cornerRadius = 20.dp,
                contentPadding = 18.dp
            ) {
                Text(
                    text = "Transactions by Service",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Distribution across Registrar service categories (Demo)",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(20.dp))

                val barColors = listOf(
                    CampusQGreenPrimary,
                    CampusQInfo,
                    CampusQGreenDark,
                    CampusQAdminPurple,
                    CampusQSuccess
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    bars.forEachIndexed { index, item ->
                        val fraction = (item.count.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
                        val barColor = barColors[index % barColors.size]

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = item.count.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CampusQTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .fillMaxHeight(fraction * 0.72f)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(barColor)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.shortLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = CampusQTextSecondary
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = CampusQOutline)
                Spacer(modifier = Modifier.height(10.dp))

                bars.forEachIndexed { index, item ->
                    val barColor = barColors[index % barColors.size]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(barColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.serviceName,
                                style = MaterialTheme.typography.bodySmall.copy(color = CampusQTextPrimary)
                            )
                        }
                        Text(
                            text = "${item.count} requests",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = CampusQTextPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * SCREEN 23 — SYSTEM SETTINGS (Purple Header per Reference)
 */
@Composable
fun AdminSystemSettingsScreen(
    settings: InstitutionSettings,
    onUpdateSettings: (InstitutionSettings) -> Unit,
    onNavigate: (String) -> Unit,
    onExitToPreviewHub: () -> Unit
) {
    var activeDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var settingsError by rememberSaveable { mutableStateOf<String?>(null) }
    var instName by rememberSaveable(settings.institutionName) { mutableStateOf(settings.institutionName) }
    var opDays by rememberSaveable(settings.operatingDays) { mutableStateOf(settings.operatingDays) }
    var opHours by rememberSaveable(settings.operatingHours) { mutableStateOf(settings.operatingHours) }
    var pushAlerts by rememberSaveable(settings.pushAlertsEnabled) { mutableStateOf(settings.pushAlertsEnabled) }

    AdminScaffoldWithDrawer(
        title = "System Settings",
        currentRoute = CampusQRoutes.ADMIN_SYSTEM_SETTINGS,
        headerColor = CampusQAdminPurple,
        onNavigate = onNavigate,
        onExitToPreviewHub = onExitToPreviewHub
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CampusQSectionHeading(title = "Registrar System Configuration")
            Spacer(modifier = Modifier.height(2.dp))

            CampusQProfileMenuItem(
                title = "Institution Information",
                subtitle = settings.institutionName,
                icon = Icons.Default.AccountBalance,
                accentColor = CampusQAdminPurple,
                onClick = { instName = settings.institutionName; settingsError = null; activeDialog = "institution" }
            )
            CampusQProfileMenuItem(
                title = "Operating Schedule",
                subtitle = "${settings.operatingDays} • ${settings.operatingHours}",
                icon = Icons.Default.Schedule,
                accentColor = CampusQAdminPurple,
                onClick = { opDays = settings.operatingDays; opHours = settings.operatingHours; settingsError = null; activeDialog = "schedule" }
            )
            CampusQProfileMenuItem(
                title = "Notification Settings",
                subtitle = "Queue call alerts and reminder preferences",
                icon = Icons.Default.NotificationsActive,
                accentColor = CampusQAdminPurple,
                onClick = { pushAlerts = settings.pushAlertsEnabled; activeDialog = "notifications" }
            )
            CampusQProfileMenuItem(
                title = "Account & Security",
                subtitle = "Role-based access & authentication overview",
                icon = Icons.Default.Security,
                accentColor = CampusQAdminPurple,
                onClick = { activeDialog = "security" }
            )
            CampusQProfileMenuItem(
                title = "Backup & Data",
                subtitle = "Data export and cloud backup preview",
                icon = Icons.Default.Backup,
                accentColor = CampusQAdminPurple,
                onClick = { activeDialog = "backup" }
            )
            CampusQProfileMenuItem(
                title = "About CampusQ",
                subtitle = "CampusQ v1.0 • Queue smarter. Wait less.",
                icon = Icons.Default.Info,
                accentColor = CampusQAdminPurple,
                onClick = { activeDialog = "about" }
            )
            CampusQProfileMenuItem(
                title = "Switch Role Preview",
                subtitle = "Return to UI Preview Role Selector",
                icon = Icons.Default.SwapHoriz,
                accentColor = CampusQAdminPurple,
                onClick = onExitToPreviewHub
            )
        }
    }

    when (activeDialog) {
        "institution" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("Institution Information (Demo)", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        settingsError?.let { Text(it, color = CampusQError) }
                        CampusQTextField(
                            value = instName,
                            onValueChange = { instName = it },
                            label = "Institution / Office Name"
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (instName.isBlank()) settingsError = "Institution name is required." else {
                                onUpdateSettings(settings.copy(institutionName = instName.trim()))
                                activeDialog = null
                            }
                        }
                    ) {
                        Text("Save Demo Setting", color = CampusQAdminPurple, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Cancel", color = CampusQTextSecondary)
                    }
                },
                containerColor = CampusQSurface
            )
        }
        "schedule" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("Operating Schedule (Demo)", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column {
                        CampusQTextField(
                            value = opDays,
                            onValueChange = { opDays = it },
                            label = "Operating Days"
                        )
                        CampusQTextField(
                            value = opHours,
                            onValueChange = { opHours = it },
                            label = "Operating Hours"
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (opDays.isBlank() || opHours.isBlank()) settingsError = "Operating days and hours are required." else {
                                onUpdateSettings(settings.copy(operatingDays = opDays.trim(), operatingHours = opHours.trim()))
                                activeDialog = null
                            }
                        }
                    ) {
                        Text("Save Demo Schedule", color = CampusQAdminPurple, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Cancel", color = CampusQTextSecondary)
                    }
                },
                containerColor = CampusQSurface
            )
        }
        "notifications" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("Notification Settings (Demo)", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Queue Call Alerts", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = pushAlerts,
                            onCheckedChange = { pushAlerts = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CampusQAdminPurple)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onUpdateSettings(settings.copy(pushAlertsEnabled = pushAlerts))
                            activeDialog = null
                        }
                    ) {
                        Text("Save", color = CampusQAdminPurple, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = CampusQSurface
            )
        }
        "security" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("Account & Security (UI Preview)", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Text(
                        "Security policies, password changes, and role claims require backend Firebase Authentication integration. No real security credentials are modified in this UI prototype.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Close", color = CampusQAdminPurple, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = CampusQSurface
            )
        }
        "backup" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("Backup & Data (UI Preview)", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Text(
                        "Cloud backups and database exports are not connected in this UI-only phase. All current data is stored in an in-memory demonstration repository.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Understood", color = CampusQAdminPurple, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = CampusQSurface
            )
        }
        "about" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("About CampusQ", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Text(
                        "CampusQ — Smart Queue & Appointment Management System\nOfficial Tagline: Queue smarter. Wait less.\n\nComplete 23-Screen Material Design 3 UI/UX Prototype for BSIT Capstone Project.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Close", color = CampusQAdminPurple, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = CampusQSurface
            )
        }
    }
}
