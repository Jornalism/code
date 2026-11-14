package com.example.campusq.ui.student

import com.example.campusq.ui.navigation.LocalServerMode
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.campusq.data.model.AppointmentSlot
import com.example.campusq.data.model.isReserved
import com.example.campusq.data.model.Appointment
import com.example.campusq.data.model.AppointmentStatus
import com.example.campusq.data.model.DemoBookingRules
import com.example.campusq.data.model.canEdit
import java.time.LocalTime
import androidx.compose.foundation.layout.imePadding
import com.example.campusq.ui.components.CampusQAppointmentCard
import com.example.campusq.ui.components.CampusQSectionHeading
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusq.R
import com.example.campusq.data.model.RegistrarService
import com.example.campusq.data.model.StudentProfile
import com.example.campusq.data.model.TransactionRecord
import com.example.campusq.data.model.TransactionType
import com.example.campusq.ui.components.CampusQAppointmentStatusChip
import com.example.campusq.ui.components.CampusQBottomNavigation
import com.example.campusq.ui.components.CampusQCard
import com.example.campusq.ui.components.CampusQConfirmationDialog
import com.example.campusq.ui.components.CampusQDateSelector
import com.example.campusq.ui.components.CampusQEmptyState
import com.example.campusq.ui.components.CampusQFilterTabs
import com.example.campusq.ui.components.CampusQPrimaryButton
import com.example.campusq.ui.components.CampusQProfileHeader
import com.example.campusq.ui.components.CampusQProfileMenuItem
import com.example.campusq.ui.components.CampusQQueueStatusChip
import com.example.campusq.ui.components.CampusQTextField
import com.example.campusq.ui.components.CampusQTimeSlotChip
import com.example.campusq.ui.components.CampusQTopAppBar
import com.example.campusq.ui.components.getServiceIcon
import com.example.campusq.ui.navigation.CampusQRoutes
import com.example.campusq.ui.theme.CampusQBackground
import com.example.campusq.ui.theme.CampusQGreenDark
import com.example.campusq.ui.theme.CampusQGreenLight
import com.example.campusq.ui.theme.CampusQError
import com.example.campusq.ui.theme.CampusQGreenPrimary
import com.example.campusq.ui.theme.CampusQSurface
import com.example.campusq.ui.theme.CampusQTextPrimary
import com.example.campusq.ui.theme.CampusQTextSecondary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * SCREEN 07 — BOOK APPOINTMENT
 */
@Composable
fun BookAppointmentScreen(
    services: List<RegistrarService>,
    preselectedService: RegistrarService?,
    onNavigateBottom: (String) -> Unit,
    onConfirmBooking: (RegistrarService, String, String) -> Unit,
    appointments: List<Appointment> = emptyList(),
    onCancelAppointment: (String) -> Unit = {},
    onRescheduleAppointment: (String, RegistrarService, String, String) -> Boolean = { _, _, _, _ -> false },
    reservedSlots: List<AppointmentSlot> = emptyList()
) {
    val isLocalServer = LocalServerMode.current
    val availableServices = services.filter { it.isAvailable && it.isAppointmentEnabled }

    var selectedServiceId by rememberSaveable(preselectedService?.id) {
        mutableStateOf(preselectedService?.id ?: availableServices.firstOrNull()?.id.orEmpty())
    }
    val selectedService = availableServices.find { it.id == selectedServiceId } ?: availableServices.firstOrNull()

    var showServiceDropdown by remember { mutableStateOf(false) }

    val initialValidDate = remember {
        var d = LocalDate.now().plusDays(1)
        while (d.dayOfWeek == DayOfWeek.SATURDAY || d.dayOfWeek == DayOfWeek.SUNDAY) {
            d = d.plusDays(1)
        }
        d
    }
    var selectedDateText by rememberSaveable { mutableStateOf(initialValidDate.toString()) }
    val selectedDate = LocalDate.parse(selectedDateText)
    var currentMonthText by rememberSaveable { mutableStateOf(YearMonth.from(initialValidDate).toString()) }
    val currentMonth = YearMonth.parse(currentMonthText)
    var viewingAppointment by remember { mutableStateOf<Appointment?>(null) }
    var editingAppointmentId by rememberSaveable { mutableStateOf<String?>(null) }
    var cancellingAppointmentId by rememberSaveable { mutableStateOf<String?>(null) }

    val timeSlots = remember {
        listOf(
            "9:00 AM" to true,
            "10:00 AM" to true,
            "11:00 AM" to true,
            "1:00 PM" to true,
            "2:00 PM" to true,
            "3:00 PM" to false // 1 disabled slot to demonstrate unavailable visual state
        )
    }
    var selectedTimeSlot by rememberSaveable { mutableStateOf("10:00 AM") }
    var showConfirmSummaryDialog by rememberSaveable { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US) }

    Scaffold(
        containerColor = CampusQBackground,
        topBar = {
            CampusQTopAppBar(title = "Book Appointment")
        },
        bottomBar = {
            CampusQBottomNavigation(
                destinations = studentBottomDestinations,
                currentRoute = CampusQRoutes.STUDENT_BOOK_APPOINTMENT,
                onNavigate = onNavigateBottom
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (appointments.isNotEmpty()) {
                CampusQSectionHeading(title = (if (isLocalServer) "Your Appointments" else "Your Demo Appointments"))
                Spacer(modifier = Modifier.height(8.dp))
                appointments.forEach { appointment ->
                    CampusQAppointmentCard(appointment = appointment, onClick = { viewingAppointment = appointment })
                    if (appointment.status.canEdit) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                editingAppointmentId = appointment.id
                                selectedServiceId = appointment.serviceId
                                selectedDateText = DemoBookingRules.parseDate(appointment.dateText).toString()
                                currentMonthText = YearMonth.from(LocalDate.parse(selectedDateText)).toString()
                                selectedTimeSlot = appointment.timeSlot
                            }) { Text("Reschedule", color = CampusQGreenDark) }
                            TextButton(onClick = { cancellingAppointmentId = appointment.id }) {
                                Text("Cancel Appointment", color = CampusQError)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            if (availableServices.isEmpty()) {
                CampusQEmptyState(title = "No Appointments Available", message = "Registrar services are not currently accepting bookings.")
            }
            if (editingAppointmentId != null) {
                Text("Rescheduling ${appointments.find { it.id == editingAppointmentId }?.referenceNumber.orEmpty()}", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { editingAppointmentId = null }) { Text("Cancel Rescheduling") }
            }
            // Step 1: Select Service
            BookingStepHeader(stepNumber = 1, title = "Select Service")
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                CampusQCard(
                    onClick = { showServiceDropdown = true },
                    contentPadding = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedService != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CampusQGreenLight,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = getServiceIcon(selectedService.iconType),
                                            contentDescription = null,
                                            tint = CampusQGreenPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = selectedService.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Est. duration: ${selectedService.estimatedMinutes} min",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Select Service",
                            tint = CampusQGreenPrimary
                        )
                    }
                }

                DropdownMenu(
                    expanded = showServiceDropdown,
                    onDismissRequest = { showServiceDropdown = false },
                    modifier = Modifier.background(CampusQSurface)
                ) {
                    availableServices.forEach { srv ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = srv.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = CampusQTextPrimary,
                                        fontWeight = if (srv.id == selectedService?.id) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            onClick = {
                                selectedServiceId = srv.id
                                showServiceDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Step 2: Select Date
            BookingStepHeader(stepNumber = 2, title = "Select Date")
            Spacer(modifier = Modifier.height(8.dp))

            CampusQDateSelector(
                selectedDate = selectedDate,
                onDateSelected = { selectedDateText = it.toString() },
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonthText = currentMonth.minusMonths(1).toString() },
                onNextMonth = { currentMonthText = currentMonth.plusMonths(1).toString() }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Step 3: Select Time
            BookingStepHeader(stepNumber = 3, title = "Select Time")
            Spacer(modifier = Modifier.height(8.dp))

            val rows = timeSlots.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { rowSlots ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSlots.forEach { (slotLabel, isSlotAvailable) ->
                            CampusQTimeSlotChip(
                                timeSlot = slotLabel,
                                isSelected = selectedTimeSlot == slotLabel,
                                isAvailable = isSlotAvailable && runCatching {
                                    DemoBookingRules.validateSchedule(selectedDate.format(DemoBookingRules.dateFormatter), slotLabel, LocalDate.now(), LocalTime.now())
                                }.isSuccess && !reservedSlots.isReserved(selectedService?.id.orEmpty(), selectedDate.format(dateFormatter), slotLabel, editingAppointmentId) && appointments.none {
                                    it.id != editingAppointmentId && it.status !in setOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW) &&
                                        it.dateText == selectedDate.format(DemoBookingRules.dateFormatter) && it.timeSlot == slotLabel
                                },
                                onClick = { selectedTimeSlot = slotLabel },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            CampusQPrimaryButton(
                text = if (editingAppointmentId == null) "Confirm Appointment" else "Confirm Rescheduling",
                enabled = selectedService != null && runCatching {
                    DemoBookingRules.validateSchedule(selectedDate.format(DemoBookingRules.dateFormatter), selectedTimeSlot, LocalDate.now(), LocalTime.now())
                }.isSuccess && !reservedSlots.isReserved(selectedService?.id.orEmpty(), selectedDate.format(dateFormatter), selectedTimeSlot, editingAppointmentId) && appointments.none {
                    it.id != editingAppointmentId && it.status !in setOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW) &&
                        it.dateText == selectedDate.format(DemoBookingRules.dateFormatter) && it.timeSlot == selectedTimeSlot
                },
                onClick = { showConfirmSummaryDialog = true }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    viewingAppointment?.let { item ->
        AlertDialog(onDismissRequest = { viewingAppointment = null }, title = { Text(item.referenceNumber) },
            text = { Text("${item.serviceName}\n${item.dateText} • ${item.timeSlot}\nStatus: ${item.status.displayName}") },
            confirmButton = { TextButton(onClick = { viewingAppointment = null }) { Text("Close") } })
    }
    if (showConfirmSummaryDialog && selectedService != null) {
        val formattedDate = selectedDate.format(dateFormatter)
        CampusQConfirmationDialog(
            title = (if (isLocalServer) "Confirm Appointment" else "Confirm Demo Appointment"),
            message = (if (isLocalServer) "Service: ${selectedService.name}\nDate: $formattedDate\nTime: $selectedTimeSlot\n\nNote: This confirms a booking on the CampusQ laptop server." else "Service: ${selectedService.name}\nDate: $formattedDate\nTime: $selectedTimeSlot\n\nNote: This confirms a local UI demonstration booking only."),
            confirmText = "Confirm Booking",
            onConfirm = {
                showConfirmSummaryDialog = false
                val editingId = editingAppointmentId
                if (editingId == null) {
                    onConfirmBooking(selectedService, formattedDate, selectedTimeSlot)
                } else if (onRescheduleAppointment(editingId, selectedService, formattedDate, selectedTimeSlot)) {
                    editingAppointmentId = null
                }
            },
            onDismiss = { showConfirmSummaryDialog = false }
        )
    }
    cancellingAppointmentId?.let { id ->
        CampusQConfirmationDialog(
            title = "Cancel Appointment?", message = (if (isLocalServer) "Cancel this appointment?" else "Cancel this local demonstration appointment?"),
            confirmText = "Cancel Appointment", isDestructive = true,
            onConfirm = { onCancelAppointment(id); cancellingAppointmentId = null },
            onDismiss = { cancellingAppointmentId = null }
        )
    }
}

@Composable
private fun BookingStepHeader(
    stepNumber: Int,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = CampusQGreenDark,
            modifier = Modifier.size(26.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

/**
 * SCREEN 08 — TRANSACTION HISTORY
 */
@Composable
fun TransactionHistoryScreen(
    transactions: List<TransactionRecord>,
    onNavigateBottom: (String) -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    val isLocalServer = LocalServerMode.current
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedRecord by remember { mutableStateOf<TransactionRecord?>(null) }

    val tabs = listOf("All", "Queue", "Appointments")
    val filteredRecords = transactions.filter { record ->
        when (selectedTabIndex) {
            1 -> record.type == TransactionType.QUEUE
            2 -> record.type == TransactionType.APPOINTMENT
            else -> true
        }
    }

    Scaffold(
        containerColor = CampusQBackground,
        topBar = {
            CampusQTopAppBar(
                title = "Transaction History",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            if (onBackClick == null) {
                CampusQBottomNavigation(
                    destinations = studentBottomDestinations,
                    currentRoute = CampusQRoutes.STUDENT_HISTORY,
                    onNavigate = onNavigateBottom
                )
            }
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

            if (filteredRecords.isEmpty()) {
                CampusQEmptyState(
                    title = "No Transaction Records",
                    message = (if (isLocalServer) "No ${tabs[selectedTabIndex].lowercase()} records found in the stored history." else "No ${tabs[selectedTabIndex].lowercase()} records found in the current demonstration state."),
                    icon = Icons.Default.History
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { record ->
                        TransactionHistoryCard(
                            record = record,
                            onClick = { selectedRecord = record }
                        )
                    }
                }
            }
        }
    }

    if (selectedRecord != null) {
        val rec = selectedRecord!!
        AlertDialog(
            onDismissRequest = { selectedRecord = null },
            title = {
                Text(
                    text = "${rec.referenceNumber} — ${rec.serviceName}",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Type: ${rec.type.name}", style = MaterialTheme.typography.bodyMedium)
                    Text("Student: ${rec.studentName}", style = MaterialTheme.typography.bodyMedium)
                    Text("Date & Time: ${rec.dateTimeText}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Status: ${rec.queueStatus?.displayName ?: rec.appointmentStatus?.displayName ?: "Completed"}${if (isLocalServer) "" else " (Demo)"}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedRecord = null }) {
                    Text("Close", color = CampusQGreenDark, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CampusQSurface
        )
    }
}

@Composable
private fun TransactionHistoryCard(
    record: TransactionRecord,
    onClick: () -> Unit
) {
    val largeText = LocalDensity.current.fontScale > 1.3f
    CampusQCard(
        onClick = onClick,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = CampusQGreenLight,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (record.type == TransactionType.QUEUE) {
                            Icons.Default.ConfirmationNumber
                        } else {
                            Icons.Default.CalendarMonth
                        },
                        contentDescription = null,
                        tint = CampusQGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.referenceNumber} — ${record.serviceName}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = record.dateTimeText,
                    style = MaterialTheme.typography.bodySmall.copy(color = CampusQTextSecondary)
                )
                if (largeText) {
                    Spacer(modifier = Modifier.height(8.dp))
                    record.queueStatus?.let { CampusQQueueStatusChip(it) }
                    record.appointmentStatus?.let { CampusQAppointmentStatusChip(it) }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (!largeText && record.queueStatus != null) {
                CampusQQueueStatusChip(status = record.queueStatus)
            } else if (!largeText && record.appointmentStatus != null) {
                CampusQAppointmentStatusChip(status = record.appointmentStatus)
            }
        }
    }
}

/**
 * SCREEN 09 — STUDENT PROFILE
 */
@Composable
fun StudentProfileScreen(
    studentProfile: StudentProfile,
    onNavigateBottom: (String) -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onLogoutToPreviewHub: () -> Unit
) {
    val isLocalServer = LocalServerMode.current
    var activeDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    var editName by rememberSaveable(studentProfile.fullName) { mutableStateOf(studentProfile.fullName) }
    var editStudentId by rememberSaveable(studentProfile.studentId) { mutableStateOf(studentProfile.studentId) }
    var profileError by rememberSaveable { mutableStateOf<String?>(null) }
    var editEmail by rememberSaveable(studentProfile.email) { mutableStateOf(studentProfile.email) }
    var notifyQueueUpdates by rememberSaveable { mutableStateOf(true) }
    var notifyAppointmentReminders by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        containerColor = CampusQBackground,
        topBar = {
            CampusQTopAppBar(title = "Profile")
        },
        bottomBar = {
            CampusQBottomNavigation(
                destinations = studentBottomDestinations,
                currentRoute = CampusQRoutes.STUDENT_PROFILE,
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
                fullName = studentProfile.fullName,
                subtitle1 = "Student ID: ${studentProfile.studentId}",
                subtitle2 = studentProfile.email,
                avatarResId = R.drawable.img_avatar_student
            )

            Spacer(modifier = Modifier.height(4.dp))

            CampusQProfileMenuItem(
                title = "Personal Information",
                subtitle = (if (isLocalServer) "View or update your student information" else "View or update demonstration student info"),
                icon = Icons.Default.Person,
                onClick = {
                    editName = studentProfile.fullName
                    editStudentId = studentProfile.studentId
                    editEmail = studentProfile.email
                    profileError = null
                    activeDialog = "personal"
                }
            )
            CampusQProfileMenuItem(
                title = "Account Settings",
                subtitle = "Academic program and security preferences",
                icon = Icons.Default.Settings,
                onClick = { activeDialog = "account" }
            )
            CampusQProfileMenuItem(
                title = "Notifications",
                subtitle = "Queue call alerts & appointment reminders",
                icon = Icons.Default.Notifications,
                onClick = { activeDialog = "notifications" }
            )
            CampusQProfileMenuItem(
                title = "Help & Support",
                subtitle = "Registrar FAQ and guidance",
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                onClick = { activeDialog = "help" }
            )
            CampusQProfileMenuItem(
                title = (if (isLocalServer) "Sign out" else "Switch Role Preview"),
                subtitle = (if (isLocalServer) "Sign out before using another account" else "Switch between Student, Staff, and Admin UI"),
                icon = Icons.Default.SwapHoriz,
                onClick = onLogoutToPreviewHub
            )
            CampusQProfileMenuItem(
                title = "About CampusQ",
                subtitle = "Queue smarter. Wait less. • BSIT Capstone UI",
                icon = Icons.Default.Info,
                onClick = { activeDialog = "about" }
            )
            CampusQProfileMenuItem(
                title = "Logout",
                subtitle = (if (isLocalServer) "Return to sign in" else "Return to UI Preview / Login entry"),
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                isDestructive = true,
                onClick = { showLogoutDialog = true }
            )
        }
    }

    when (activeDialog) {
        "personal" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text((if (isLocalServer) "Personal Information" else "Personal Information (Demo)"), style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        profileError?.let { Text(it, color = CampusQError) }
                        CampusQTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = "Full Name"
                        )
                        CampusQTextField(
                            value = editStudentId,
                            onValueChange = { editStudentId = it },
                            label = "Student ID"
                        )
                        CampusQTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = "Email Address"
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            profileError = when {
                                editName.trim().length < 3 -> "Enter your full name."
                                editStudentId.trim().length < 5 -> "Enter a valid Student ID."
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(editEmail.trim()).matches() -> "Enter a valid email address."
                                else -> null
                            }
                            if (profileError == null) {
                                onUpdateProfile(editName, editStudentId, editEmail)
                                activeDialog = null
                            }
                        }
                    ) {
                        Text((if (isLocalServer) "Save Info" else "Save Demo Info"), color = CampusQGreenDark, fontWeight = FontWeight.Bold)
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
        "account" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text((if (isLocalServer) "Account Settings" else "Account Settings (Demo)"), style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Program: ${studentProfile.program}", style = MaterialTheme.typography.bodyMedium)
                        Text("Year Level: ${studentProfile.yearLevel}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            (if (isLocalServer) "Contact the authorized Registrar operator to reset a password. No recovery email is sent." else "Password & credential changes are disabled in this UI-only prototype."),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Close", color = CampusQGreenDark, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = CampusQSurface
            )
        }
        "notifications" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text((if (isLocalServer) "Notification Preferences" else "Notification Preferences (Demo)"), style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isLocalServer) "Push alerts unavailable offline" else "Queue Turn Alerts", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = !isLocalServer && notifyQueueUpdates,
                                onCheckedChange = { notifyQueueUpdates = it },
                                enabled = !isLocalServer,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CampusQGreenPrimary)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isLocalServer) "Timed reminders unavailable offline" else "Appointment Reminders", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = !isLocalServer && notifyAppointmentReminders,
                                onCheckedChange = { notifyAppointmentReminders = it },
                                enabled = !isLocalServer,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CampusQGreenPrimary)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Done", color = CampusQGreenDark, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = CampusQSurface
            )
        }
        "help" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("Help & Support", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Text(
                        "• How do I join a queue?\nTap 'Join Queue' on the Home screen and choose your Registrar service.\n\n• When should I arrive?\nPlease be near the Registrar Office when there are 2 or fewer people ahead of you.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Got it", color = CampusQGreenDark, fontWeight = FontWeight.Bold)
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
                        (if (isLocalServer) "CampusQ — Smart Queue & Appointment Management System\nTagline: Queue smarter. Wait less.\n\nBSIT Academic Capstone Project local-network application." else "CampusQ — Smart Queue & Appointment Management System\nTagline: Queue smarter. Wait less.\n\nBSIT Academic Capstone Project UI/UX Prototype."),
                        style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Close", color = CampusQGreenDark, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = CampusQSurface
            )
        }
    }

    if (showLogoutDialog) {
        CampusQConfirmationDialog(
            title = (if (isLocalServer) "Sign out?" else "Exit Student Demo?"),
            message = (if (isLocalServer) "Sign out of this account and return to the login screen?" else "This will return you to the CampusQ UI Preview / Login screen. No remote session is active."),
            confirmText = (if (isLocalServer) "Sign out" else "Exit Demo"),
            isDestructive = true,
            onConfirm = {
                showLogoutDialog = false
                onLogoutToPreviewHub()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}
