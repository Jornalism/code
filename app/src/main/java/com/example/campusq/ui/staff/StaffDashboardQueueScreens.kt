package com.example.campusq.ui.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.campusq.ui.theme.CampusQGreenDark
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusq.data.model.QueueStatus
import com.example.campusq.data.model.QueueTicket
import com.example.campusq.data.model.TransactionRecord
import com.example.campusq.data.model.TransactionType
import com.example.campusq.ui.components.CampusQBottomNavigation
import com.example.campusq.ui.components.CampusQCard
import com.example.campusq.ui.components.CampusQConfirmationDialog
import com.example.campusq.ui.components.CampusQDestructiveButton
import com.example.campusq.ui.components.CampusQEmptyState
import com.example.campusq.ui.components.CampusQFilterTabs
import com.example.campusq.ui.components.CampusQNavDestination
import com.example.campusq.ui.components.CampusQPrimaryButton
import com.example.campusq.ui.components.CampusQQueueStatusChip
import com.example.campusq.ui.components.CampusQSecondaryButton
import com.example.campusq.ui.components.CampusQSectionHeading
import com.example.campusq.ui.components.CampusQStatCard
import com.example.campusq.ui.components.CampusQTopAppBar
import com.example.campusq.ui.navigation.CampusQRoutes
import com.example.campusq.ui.theme.CampusQBackground
import com.example.campusq.ui.theme.CampusQError
import com.example.campusq.ui.theme.CampusQGreenLight
import com.example.campusq.ui.theme.CampusQGreenPrimary
import com.example.campusq.ui.theme.CampusQInfo
import com.example.campusq.ui.theme.CampusQOutline
import com.example.campusq.ui.theme.CampusQSuccess
import com.example.campusq.ui.theme.CampusQTextPrimary
import com.example.campusq.ui.theme.CampusQTextSecondary
import com.example.campusq.ui.theme.CampusQWarningDark

val staffBottomDestinations = listOf(
    CampusQNavDestination(CampusQRoutes.STAFF_DASHBOARD, "Dashboard", Icons.Default.Dashboard),
    CampusQNavDestination(CampusQRoutes.STAFF_QUEUE_MANAGEMENT, "Queue", Icons.AutoMirrored.Filled.FormatListBulleted),
    CampusQNavDestination(CampusQRoutes.STAFF_APPOINTMENTS, "Appointments", Icons.Default.CalendarMonth),
    CampusQNavDestination(CampusQRoutes.STAFF_SERVICE_STATUS, "History", Icons.Default.History),
    CampusQNavDestination(CampusQRoutes.STAFF_PROFILE, "Profile", Icons.Default.Person)
)

/**
 * SCREEN 10 — STAFF DASHBOARD
 */
@Composable
fun StaffDashboardScreen(
    currentDateText: String,
    tickets: List<QueueTicket>,
    transactions: List<TransactionRecord>,
    onNavigateBottom: (String) -> Unit,
    onCallNext: () -> QueueTicket?,
    onOpenQueueManagement: () -> Unit,
    onOpenAppointments: () -> Unit,
    onOpenServiceStatus: () -> Unit,
    onOpenTicketDetails: (String) -> Unit,
    onSwitchRolePreview: () -> Unit,
    counterName: String = "Window 1"
) {
    var showCallNextDialog by rememberSaveable { mutableStateOf(false) }

    val waitingCount = tickets.count { it.status == QueueStatus.WAITING }
    val inServiceCount = tickets.count { it.status == QueueStatus.SERVING || it.status == QueueStatus.CALLED }
    val completedCount = transactions.count {
        it.type == TransactionType.QUEUE && it.queueStatus == QueueStatus.COMPLETED
    }
    val noShowCount = tickets.count { it.status == QueueStatus.NO_SHOW || it.status == QueueStatus.SKIPPED }

    val nextWaitingTicket = tickets.firstOrNull { it.status == QueueStatus.WAITING }

    Scaffold(
        containerColor = CampusQBackground,
        topBar = {
            CampusQTopAppBar(
                title = "Registrar Dashboard",
                actions = {
                    IconButton(onClick = onSwitchRolePreview) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Role Preview",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            CampusQBottomNavigation(
                destinations = staffBottomDestinations,
                currentRoute = CampusQRoutes.STAFF_DASHBOARD,
                onNavigate = onNavigateBottom
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today • $currentDateText",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = CampusQTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CampusQGreenLight
                ) {
                    Text(
                        text = "$counterName Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CampusQGreenDark,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Compact Statistic Cards: Waiting, In Service, Completed, No Show
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CampusQStatCard(
                    title = "Waiting",
                    countValue = waitingCount.toString(),
                    accentColor = CampusQWarningDark,
                    icon = Icons.Default.HourglassEmpty,
                    onClick = onOpenQueueManagement,
                    modifier = Modifier.weight(1f)
                )
                CampusQStatCard(
                    title = "In Service",
                    countValue = inServiceCount.toString(),
                    accentColor = CampusQInfo,
                    icon = Icons.Default.Sync,
                    onClick = onOpenServiceStatus,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CampusQStatCard(
                    title = "Completed",
                    countValue = completedCount.toString(),
                    accentColor = CampusQSuccess,
                    icon = Icons.Default.CheckCircle,
                    onClick = onOpenServiceStatus,
                    modifier = Modifier.weight(1f)
                )
                CampusQStatCard(
                    title = "No Show",
                    countValue = noShowCount.toString(),
                    accentColor = CampusQError,
                    icon = Icons.Default.PersonOff,
                    onClick = onOpenQueueManagement,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            CampusQSectionHeading(title = "Quick Actions")
            Spacer(modifier = Modifier.height(10.dp))

            CampusQPrimaryButton(
                text = if (nextWaitingTicket != null) {
                    "Call Next (${nextWaitingTicket.ticketNumber} — ${nextWaitingTicket.serviceName})"
                } else {
                    "Call Next (No Waiting Tickets)"
                },
                leadingIcon = Icons.Default.Campaign,
                enabled = nextWaitingTicket != null,
                onClick = { showCallNextDialog = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CampusQSecondaryButton(
                    text = "View Queue",
                    onClick = onOpenQueueManagement,
                    modifier = Modifier.weight(1f)
                )
                CampusQSecondaryButton(
                    text = "Appointments",
                    onClick = onOpenAppointments,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            CampusQSecondaryButton(
                text = "Transaction History / Service Status",
                onClick = onOpenServiceStatus
            )

            Spacer(modifier = Modifier.height(20.dp))

            CampusQSectionHeading(
                title = "Up Next in Queue",
                actionText = "Manage All",
                onActionClick = onOpenQueueManagement
            )
            Spacer(modifier = Modifier.height(10.dp))

            val activeQueuePreview = tickets.filter {
                it.status == QueueStatus.WAITING || it.status == QueueStatus.CALLED || it.status == QueueStatus.SERVING
            }.take(3)

            if (activeQueuePreview.isEmpty()) {
                CampusQEmptyState(
                    title = "Queue is Clear",
                    message = "All waiting demonstration tickets have been processed."
                )
            } else {
                activeQueuePreview.forEach { ticket ->
                    StaffQueueRowCard(
                        ticket = ticket,
                        onRowClick = { onOpenTicketDetails(ticket.id) },
                        onCallClick = null
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showCallNextDialog && nextWaitingTicket != null) {
        CampusQConfirmationDialog(
            title = "Call Next Ticket?",
            message = "Call demonstration ticket ${nextWaitingTicket.ticketNumber} (${nextWaitingTicket.studentName} — ${nextWaitingTicket.serviceName}) to $counterName?",
            confirmText = "Call Ticket",
            onConfirm = {
                showCallNextDialog = false
                val called = onCallNext()
                if (called != null) {
                    onOpenTicketDetails(called.id)
                }
            },
            onDismiss = { showCallNextDialog = false }
        )
    }
}

/**
 * SCREEN 11 — QUEUE MANAGEMENT
 */
@Composable
fun StaffQueueManagementScreen(
    tickets: List<QueueTicket>,
    onNavigateBottom: (String) -> Unit,
    onOpenTicketDetails: (String) -> Unit,
    onCallTicket: (QueueTicket) -> Unit
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Waiting", "Called", "Serving")

    val filteredTickets = tickets.filter { ticket ->
        when (selectedTabIndex) {
            0 -> ticket.status == QueueStatus.WAITING || ticket.status == QueueStatus.SKIPPED
            1 -> ticket.status == QueueStatus.CALLED
            else -> ticket.status == QueueStatus.SERVING
        }
    }

    Scaffold(
        containerColor = CampusQBackground,
        topBar = {
            CampusQTopAppBar(title = "Queue Management")
        },
        bottomBar = {
            CampusQBottomNavigation(
                destinations = staffBottomDestinations,
                currentRoute = CampusQRoutes.STAFF_QUEUE_MANAGEMENT,
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
                    title = "No ${tabs[selectedTabIndex]} Tickets",
                    message = "There are currently no demonstration tickets in the ${tabs[selectedTabIndex].lowercase()} state."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredTickets, key = { it.id }) { ticket ->
                        StaffQueueRowCard(
                            ticket = ticket,
                            onRowClick = { onOpenTicketDetails(ticket.id) },
                            onCallClick = if (ticket.status == QueueStatus.WAITING) {
                                { onCallTicket(ticket) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StaffQueueRowCard(
    ticket: QueueTicket,
    onRowClick: () -> Unit,
    onCallClick: (() -> Unit)?
) {
    CampusQCard(
        onClick = onRowClick,
        contentPadding = 14.dp
    ) {
        if (LocalDensity.current.fontScale > 1.3f) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(ticket.ticketNumber, style = MaterialTheme.typography.titleMedium, color = CampusQGreenDark)
                CampusQQueueStatusChip(ticket.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(ticket.studentName, style = MaterialTheme.typography.titleMedium)
            Text(ticket.serviceName, style = MaterialTheme.typography.bodyMedium)
            Text("Wait: ${ticket.waitingDurationText}", style = MaterialTheme.typography.bodySmall)
            if (onCallClick != null) {
                Spacer(modifier = Modifier.height(8.dp))
                CampusQPrimaryButton(text = "Call", onClick = onCallClick)
            }
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
                    shape = RoundedCornerShape(12.dp),
                    color = CampusQGreenLight,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = ticket.ticketNumber,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CampusQGreenDark,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ticket.studentName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = ticket.serviceName,
                        style = MaterialTheme.typography.bodySmall.copy(color = CampusQTextPrimary)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = CampusQTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Wait: ${ticket.waitingDurationText}",
                            style = MaterialTheme.typography.labelSmall.copy(color = CampusQTextSecondary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (onCallClick != null) {
                Button(
                    onClick = onCallClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CampusQGreenDark,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Call", style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp))
                }
            } else {
                CampusQQueueStatusChip(status = ticket.status)
            }
        }
        }
    }
}

/**
 * SCREEN 12 — STAFF TICKET DETAILS / ACTIONS
 */
@Composable
fun StaffTicketDetailsScreen(
    ticket: QueueTicket?,
    onBackClick: () -> Unit,
    onUpdateStatus: (String, QueueStatus) -> Unit,
    onNavigateToServiceInProgress: (String) -> Unit
) {
    var pendingStatusTransition by rememberSaveable { mutableStateOf<QueueStatus?>(null) }

    Scaffold(
        containerColor = CampusQBackground,
        topBar = {
            CampusQTopAppBar(
                title = "Ticket Details",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (ticket == null) {
                CampusQEmptyState(
                    title = "Ticket Not Found",
                    message = "The requested demonstration ticket could not be loaded.",
                    actionButtonText = "Go Back",
                    onActionClick = onBackClick
                )
            } else {
                CampusQCard(
                    cornerRadius = 20.dp,
                    contentPadding = 20.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Queue Ticket",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        CampusQQueueStatusChip(status = ticket.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = ticket.ticketNumber,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CampusQGreenPrimary
                        )
                    )
                    Text(
                        text = ticket.serviceName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = CampusQOutline)
                    Spacer(modifier = Modifier.height(14.dp))

                    TicketDetailRow("Student Name", ticket.studentName)
                    TicketDetailRow("Student ID", ticket.studentId)
                    TicketDetailRow("Queue Position", "#${ticket.positionInLine}")
                    TicketDetailRow("Time Joined", ticket.timeJoined)
                    TicketDetailRow("Waiting Duration", ticket.waitingDurationText)
                    if (ticket.startTime != null) {
                        TicketDetailRow("Service Started", ticket.startTime)
                    }
                    if (ticket.completedTime != null) {
                        TicketDetailRow("Completed Time", ticket.completedTime)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                CampusQSectionHeading(title = "Staff Actions (State-Aware Demo)")
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Only valid state transitions for ${ticket.status.displayName} are enabled below:",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(14.dp))

                // WAITING -> Call
                CampusQPrimaryButton(
                    text = "Call Ticket",
                    leadingIcon = Icons.Default.Campaign,
                    enabled = ticket.status == QueueStatus.WAITING,
                    onClick = { pendingStatusTransition = QueueStatus.CALLED }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // CALLED -> Start Service
                CampusQPrimaryButton(
                    text = "Start Service",
                    leadingIcon = Icons.Default.PlayArrow,
                    enabled = ticket.status == QueueStatus.CALLED,
                    onClick = {
                        onUpdateStatus(ticket.id, QueueStatus.SERVING)
                        onNavigateToServiceInProgress(ticket.id)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // SERVING -> Complete Service or Open Active Session
                if (ticket.status == QueueStatus.SERVING) {
                    CampusQSecondaryButton(
                        text = "Open Service in Progress Screen",
                        onClick = { onNavigateToServiceInProgress(ticket.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                CampusQPrimaryButton(
                    text = "Complete Service",
                    leadingIcon = Icons.Default.CheckCircle,
                    enabled = ticket.status == QueueStatus.SERVING,
                    onClick = { pendingStatusTransition = QueueStatus.COMPLETED }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // CALLED -> Skip
                    CampusQSecondaryButton(
                        text = "Skip",
                        leadingIcon = Icons.Default.SkipNext,
                        enabled = ticket.status == QueueStatus.CALLED,
                        onClick = { pendingStatusTransition = QueueStatus.SKIPPED },
                        modifier = Modifier.weight(1f)
                    )
                    // SKIPPED -> Recall
                    CampusQSecondaryButton(
                        text = "Recall",
                        leadingIcon = Icons.Default.Replay,
                        enabled = ticket.status == QueueStatus.SKIPPED,
                        onClick = { pendingStatusTransition = QueueStatus.CALLED },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (pendingStatusTransition != null && ticket != null) {
        val target = pendingStatusTransition!!
        CampusQConfirmationDialog(
            title = "Confirm Status Update",
            message = "Update demonstration ticket ${ticket.ticketNumber} (${ticket.studentName}) to ${target.displayName}?",
            confirmText = "Confirm",
            onConfirm = {
                pendingStatusTransition = null
                onUpdateStatus(ticket.id, target)
            },
            onDismiss = { pendingStatusTransition = null }
        )
    }
}

@Composable
private fun TicketDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextSecondary),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = CampusQTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/**
 * SCREEN 13 — SERVICE IN PROGRESS
 */
@Composable
fun ServiceInProgressScreen(
    ticket: QueueTicket?,
    onBackClick: () -> Unit,
    onCompleteService: (String) -> Unit,
    onMarkNoShowOrCancel: (String) -> Unit,
    counterName: String = "Window 1 (Registrar)"
) {
    var showCompleteDialog by rememberSaveable { mutableStateOf(false) }
    var showNoShowDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = CampusQBackground,
        topBar = {
            CampusQTopAppBar(
                title = "Service in Progress",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (ticket == null) {
                CampusQEmptyState(
                    title = "No Ticket in Service",
                    message = "Select a ticket from Queue Management to start service.",
                    actionButtonText = "Back to Queue",
                    onActionClick = onBackClick
                )
            } else {
                // Ticket Summary Card
                CampusQCard(
                    cornerRadius = 20.dp,
                    contentPadding = 20.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CampusQQueueStatusChip(status = ticket.status)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = ticket.ticketNumber,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 44.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CampusQGreenPrimary
                            )
                        )
                        Text(
                            text = ticket.serviceName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = CampusQOutline)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Student Information
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CampusQGreenLight,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = CampusQGreenPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = ticket.studentName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Student ID: ${ticket.studentId}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CampusQOutline)
                    Spacer(modifier = Modifier.height(14.dp))

                    TicketDetailRow("Start Time", ticket.startTime ?: "Not started")
                    TicketDetailRow("Current Status", ticket.status.displayName)
                    TicketDetailRow("Counter", counterName)
                }

                Spacer(modifier = Modifier.height(24.dp))

                CampusQPrimaryButton(
                    text = "Complete Service",
                    leadingIcon = Icons.Default.CheckCircle,
                    enabled = ticket.status == QueueStatus.SERVING,
                    onClick = { showCompleteDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                CampusQDestructiveButton(
                    text = "Mark No Show / Cancel (Demo)",
                    enabled = ticket.status != QueueStatus.COMPLETED,
                    onClick = { showNoShowDialog = true }
                )
            }
        }
    }

    if (showCompleteDialog && ticket != null) {
        CampusQConfirmationDialog(
            title = "Complete Service?",
            message = "Mark demonstration ticket ${ticket.ticketNumber} (${ticket.serviceName}) as COMPLETED? This will update local history and staff/admin screens.",
            confirmText = "Complete Service",
            onConfirm = {
                showCompleteDialog = false
                onCompleteService(ticket.id)
            },
            onDismiss = { showCompleteDialog = false }
        )
    }

    if (showNoShowDialog && ticket != null) {
        CampusQConfirmationDialog(
            title = "Mark Ticket as No Show?",
            message = "Mark demonstration ticket ${ticket.ticketNumber} as NO_SHOW in the local demo state?",
            confirmText = "Mark No Show",
            isDestructive = true,
            onConfirm = {
                showNoShowDialog = false
                onMarkNoShowOrCancel(ticket.id)
            },
            onDismiss = { showNoShowDialog = false }
        )
    }
}
