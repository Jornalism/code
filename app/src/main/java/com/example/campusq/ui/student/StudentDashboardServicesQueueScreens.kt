package com.example.campusq.ui.student



import com.example.campusq.R
import com.example.campusq.data.model.NotificationItem
import com.example.campusq.data.model.QueueStatus
import com.example.campusq.data.model.QueueTicket
import com.example.campusq.data.model.RegistrarService
import com.example.campusq.data.model.StudentProfile
import com.example.campusq.ui.components.CampusQBottomNavigation
import com.example.campusq.ui.components.CampusQCard
import com.example.campusq.ui.components.CampusQConfirmationDialog
import com.example.campusq.ui.components.CampusQDemoLabel
import com.example.campusq.ui.components.CampusQDestructiveButton
import com.example.campusq.ui.components.CampusQEmptyState
import com.example.campusq.ui.components.CampusQNavDestination
import com.example.campusq.ui.components.CampusQPrimaryButton
import com.example.campusq.ui.components.CampusQQueueProgressIndicator
import com.example.campusq.ui.components.CampusQQueueStatusChip
import com.example.campusq.ui.components.CampusQSearchField
import com.example.campusq.ui.components.CampusQSecondaryButton
import com.example.campusq.ui.components.CampusQSectionHeading
import com.example.campusq.ui.components.CampusQServiceCard
import com.example.campusq.ui.components.CampusQTicketCard




/**
 * SCREEN 04 — STUDENT HOME DASHBOARD
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeDashboardScreen(
    studentProfile: StudentProfile,
    activeTicket: QueueTicket?,
    notifications: List<NotificationItem>,
    onNavigateBottom: (String) -> Unit,
    onOpenServices: () -> Unit,
    onOpenBookAppointment: () -> Unit,
    onOpenQueueStatus: () -> Unit,
    onOpenHistory: () -> Unit,
    onMarkNotificationsRead: () -> Unit,
    onRestoreSampleTicket: () -> Unit,
    onSwitchRolePreview: () -> Unit
) {
    val isLocalServer = LocalServerMode.current
    var showNotificationsSheet by rememberSaveable { mutableStateOf(false) }
    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        containerColor = CampusQBackground,
        bottomBar = {
            CampusQBottomNavigation(
                destinations = studentBottomDestinations,
                currentRoute = CampusQRoutes.STUDENT_HOME,
                onNavigate = onNavigateBottom
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // Green top header with student avatar & greeting
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = CampusQGreenDark,
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_avatar_student),
                            contentDescription = "Student Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Good morning,",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                CampusQDemoLabel(isOnDarkHeader = true)
                            }
                            Text(
                                text = "${studentProfile.fullName}!",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onSwitchRolePreview) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = (if (isLocalServer) "Sign out" else "Switch Role Preview"),
                                tint = Color.White
                            )
                        }
                    

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Active Queue Ticket or No-Active-Ticket State
                if (activeTicket != null && activeTicket.status.isActive) {
                    CampusQTicketCard(
                        ticket = activeTicket,
                        onClick = onOpenQueueStatus
                    )
                } else {
                    CampusQEmptyState(
                        title = "No Active Queue Ticket",
                        message = (if (isLocalServer) "You do not have an active queue ticket right now. Join a Registrar queue or restore the R-001 ticket." else "You do not have an active queue ticket right now. Join a Registrar queue or restore the R-001 demonstration ticket."),
                        icon = Icons.Default.ConfirmationNumber,
                        actionButtonText = "Join Registrar Queue",
                        onActionClick = onOpenServices
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!isLocalServer) TextButton(
                        onClick = onRestoreSampleTicket,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
             
                Spacer(modifier = Modifier.height(20.dp))

                CampusQSectionHeading(title = "Quick Actions")

                Spacer(modifier = Modifier.height(10.dp))

                // Prominent Quick Action Cards: Join Queue & Book Appointment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProminentQuickActionTile(
                        title = "Join Queue",
                        subtitle = "Get a ticket",
                        icon = Icons.Default.AddCircleOutline,
                        isPrimary = true,
                        onClick = onOpenServices,
                        modifier = Modifier.weight(1f)
                    )
                    ProminentQuickActionTile(
                        title = "Book Appointment",
                        subtitle = "Schedule visit",
                        icon = Icons.Default.EventAvailable,
                        isPrimary = false,
                        onClick = onOpenBookAppointment,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Smaller Softly Tinted Green Icon Actions: Services, History, Notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
               
                        Image(
                            painter = painterResource(id = R.drawable.img_campus_building),
                            contentDescription = "Campus Building Illustration",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(12.dp))
                    
                        text = (if (isLocalServer) "Notifications" else "Notifications (Demo)"),
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = onMarkNotificationsRead) {
                        Text("Mark all read", color = CampusQGreenDark)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                notifications.forEach { notif ->
                    CampusQCard(
                        modifier = Modifier.padding(vertical = 4.dp),
                        containerColor = if (notif.isRead) CampusQSurface else CampusQGreenLight.copy(alpha = 0.5f),
                        contentPadding = 12.dp
                    ) {
                        Text(
                            text = notif.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = notif.message,
                            style = MaterialTheme.typography.bodySmall.copy(color = CampusQTextPrimary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notif.timestamp,
                            style = MaterialTheme.typography.labelSmall.copy(color = CampusQTextSecondary)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProminentQuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
 
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = CircleShape,
                color = if (isPrimary) Color.White.copy(alpha = 0.2f) else Color.White,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = titleColor,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = subColor)
            )
        }
    }
}

@Composable
private fun SmallQuickActionTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),

                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = CampusQGreenDark,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * SCREEN 05 — REGISTRAR SERVICES
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarServicesScreen(
    services: List<RegistrarService>,
    onBackClick: () -> Unit,
    onJoinQueueForService: (RegistrarService) -> Unit,
    onBookAppointmentForService: (RegistrarService) -> Unit
) {
    val isLocalServer = LocalServerMode.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedServiceId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedService = services.find { it.id == selectedServiceId }

    val filteredServices = services.filter {
        searchQuery.isBlank() ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = CampusQBackground,
        topBar = {
            CampusQTopAppBar(
                title = "Registrar Services",
                onBackClick = onBackClick
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

            CampusQSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search services..."
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredServices.isEmpty()) {
                CampusQEmptyState(
                    title = "No matching services",
                    message = "No Registrar services matched \"$searchQuery\". Try clearing your search filter.",
                    actionButtonText = "Clear Search",
                    onActionClick = { searchQuery = "" }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredServices, key = { it.id }) { service ->
                        CampusQServiceCard(
                            service = service,
                            onClick = { selectedServiceId = service.id }
                        )
                    }
                }
            }
        }
    }

    if (selectedService != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedServiceId = null },
            containerColor = CampusQSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CampusQGreenLight,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getServiceIcon(selectedService.iconType),
                                contentDescription = null,
                                tint = CampusQGreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
       

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Documentary Requirements:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                selectedService.requirements.forEach { req ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CampusQGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = req,
                            style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!selectedService.isAvailable) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CampusQBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (if (isLocalServer) "This service is currently disabled in Admin Service Management." else "This service is currently disabled in Admin Service Management (Demo)."),
                            style = MaterialTheme.typography.bodySmall.copy(color = CampusQTextSecondary),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    CampusQPrimaryButton(
                        text = (if (isLocalServer) "Join Queue" else "Join Queue (Demo)"),
                        enabled = selectedService.isQueueEnabled,
                        onClick = {
                            val srv = selectedService
                            selectedServiceId = null
                            onJoinQueueForService(srv)
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    CampusQSecondaryButton(
                        text = (if (isLocalServer) "Book Appointment" else "Book Appointment (Demo)"),
                        enabled = selectedService.isAppointmentEnabled,
                        onClick = {
                            val srv = selectedService
                            selectedServiceId = null
                            onBookAppointmentForService(srv)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * SCREEN 06 — QUEUE STATUS
 */
@Composable
fun QueueStatusScreen(
    activeTicket: QueueTicket?,
    currentlyServingTicketNumber: String,
    onNavigateBottom: (String) -> Unit,
    onCancelTicket: (String) -> Unit,
    onJoinNewQueue: () -> Unit,
    onRestoreSampleTicket: () -> Unit
) {
    val isLocalServer = LocalServerMode.current
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = CampusQBackground,
        topBar = {
            CampusQTopAppBar(title = "Queue Status")
        },
        bottomBar = {
            CampusQBottomNavigation(
                destinations = studentBottomDestinations,
                currentRoute = CampusQRoutes.STUDENT_QUEUE_STATUS,
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (activeTicket == null) {
                CampusQEmptyState(
                    title = "No Active Queue Ticket",
                    message = "Join a Registrar service queue to track your live position in line.",
                    icon = Icons.Default.ConfirmationNumber,
                    actionButtonText = "Browse Registrar Services",
                    onActionClick = onJoinNewQueue
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (!isLocalServer) CampusQSecondaryButton(
                    text = "Restore R-001 Demo Ticket",
                    onClick = onRestoreSampleTicket
                )
            } else {
                // Primary Queue Card
                CampusQCard(
                    cornerRadius = 20.dp,
                    contentPadding = 20.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CampusQQueueStatusChip(status = activeTicket.status)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = activeTicket.ticketNumber,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 44.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CampusQGreenPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeTicket.serviceName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = (if (isLocalServer) "Joined at ${activeTicket.timeJoined} • Server record" else "Joined at ${activeTicket.timeJoined} • Demo State"),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Central Circular Indicator
                        CampusQQueueProgressIndicator(
                            peopleAhead = activeTicket.peopleAhead,
                            label = if (activeTicket.status == QueueStatus.SERVING) "In Service" else "People ahead"
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = CampusQOutline)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Est. Wait Time",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${activeTicket.estimatedWaitMinutes} min",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CampusQTextPrimary
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Now Serving",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = currentlyServingTicketNumber,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CampusQGreenDark
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Updated",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Just now",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CampusQTextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Timeline Card
                CampusQCard(contentPadding = 18.dp) {
                    Text(
                        text = "Queue Progress",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val currentStage = when (activeTicket.status) {
                        QueueStatus.WAITING, QueueStatus.SKIPPED -> 1
                        QueueStatus.CALLED -> 2
                        QueueStatus.SERVING -> 3
                        QueueStatus.COMPLETED -> 4
                        QueueStatus.CANCELLED, QueueStatus.NO_SHOW, QueueStatus.EXPIRED -> 0
                    }

                    val stages = listOf(
                        "Joined Queue" to "Ticket ${activeTicket.ticketNumber} issued at ${activeTicket.timeJoined}",
                        "Waiting for your turn" to "${activeTicket.peopleAhead} people ahead in line",
                        "To be called" to "Proceed to Registrar Window when called",
                        "In Service" to "Registrar staff processing your request",
                        "Completed" to "Transaction finished"
                    )

                    stages.forEachIndexed { index, (stageTitle, stageSubtitle) ->
                        val isDoneOrCurrent = index <= currentStage && currentStage > 0
                        TimelineStageRow(
                            title = stageTitle,
                            subtitle = stageSubtitle,
                            isDoneOrCurrent = isDoneOrCurrent,
                            isLast = index == stages.lastIndex
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (activeTicket.status in setOf(QueueStatus.WAITING, QueueStatus.CALLED, QueueStatus.SKIPPED)) {
                    CampusQDestructiveButton(
                        text = "Cancel Ticket",
                        onClick = { showCancelDialog = true }
                    )
                } else {
                    if (!isLocalServer) CampusQSecondaryButton(
                        text = "Restore R-001 to WAITING (Demo)",
                        onClick = onRestoreSampleTicket
                    )
                }
            }
        }
    }

    if (showCancelDialog && activeTicket != null) {
        CampusQConfirmationDialog(
            title = "Cancel Queue Ticket?",
            message = (if (isLocalServer) "Are you sure you want to cancel ticket ${activeTicket.ticketNumber} for ${activeTicket.serviceName}?" else "Are you sure you want to cancel demonstration ticket ${activeTicket.ticketNumber} for ${activeTicket.serviceName}?"),
            confirmText = "Cancel Ticket",
            isDestructive = true,
            onConfirm = {
                showCancelDialog = false
                onCancelTicket(activeTicket.id)
            },
            onDismiss = { showCancelDialog = false }
        )
    }
}

@Composable
private fun TimelineStageRow(
    title: String,
    subtitle: String,
    isDoneOrCurrent: Boolean,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isDoneOrCurrent) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isDoneOrCurrent) CampusQGreenPrimary else CampusQTextMuted,
                modifier = Modifier.size(22.dp)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(if (isDoneOrCurrent) CampusQGreenPrimary else CampusQOutline)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isDoneOrCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = if (isDoneOrCurrent) CampusQTextPrimary else CampusQTextSecondary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isDoneOrCurrent) CampusQTextSecondary else CampusQTextMuted
                )
            )
        }
    }
}
