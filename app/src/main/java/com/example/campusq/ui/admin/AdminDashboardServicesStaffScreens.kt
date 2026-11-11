package com.example.campusq.ui.admin

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusq.R
import com.example.campusq.data.model.QueueStatus
import com.example.campusq.data.model.QueueTicket
import com.example.campusq.data.model.RegistrarService
import com.example.campusq.data.model.ServiceIconType
import com.example.campusq.data.model.StaffProfile
import com.example.campusq.data.model.TransactionRecord
import com.example.campusq.ui.components.CampusQBottomNavigation
import com.example.campusq.ui.components.CampusQCard
import com.example.campusq.ui.components.CampusQConfirmationDialog
import com.example.campusq.ui.components.CampusQDemoLabel
import com.example.campusq.ui.components.CampusQEmptyState
import com.example.campusq.ui.components.CampusQNavDestination
import com.example.campusq.ui.components.CampusQPrimaryButton
import com.example.campusq.ui.components.CampusQSearchField
import com.example.campusq.ui.components.CampusQSecondaryButton
import com.example.campusq.ui.components.CampusQSectionHeading
import com.example.campusq.ui.components.CampusQStatCard
import com.example.campusq.ui.components.CampusQStatusChip
import com.example.campusq.ui.components.CampusQTextField
import com.example.campusq.ui.components.CampusQTopAppBar
import com.example.campusq.ui.components.getServiceIcon
import com.example.campusq.ui.navigation.CampusQRoutes
import com.example.campusq.ui.theme.CampusQAdminPurple
import com.example.campusq.ui.theme.CampusQAdminPurpleDark
import com.example.campusq.ui.theme.CampusQAdminPurpleLight
import com.example.campusq.ui.theme.CampusQBackground
import com.example.campusq.ui.theme.CampusQError
import com.example.campusq.ui.theme.CampusQGreenDark
import com.example.campusq.ui.theme.CampusQGreenLight
import com.example.campusq.ui.theme.CampusQGreenPrimary
import com.example.campusq.ui.theme.CampusQInfo
import com.example.campusq.ui.theme.CampusQNeutralStatus
import com.example.campusq.ui.theme.CampusQNeutralStatusBg
import com.example.campusq.ui.theme.CampusQOutline
import com.example.campusq.ui.theme.CampusQSuccess
import com.example.campusq.ui.theme.CampusQSuccessBg
import com.example.campusq.ui.theme.CampusQSurface
import com.example.campusq.ui.theme.CampusQTextPrimary
import com.example.campusq.ui.theme.CampusQTextSecondary
import com.example.campusq.ui.theme.CampusQWarningDark
import kotlinx.coroutines.launch

val adminBottomDestinations = listOf(
    CampusQNavDestination(CampusQRoutes.ADMIN_DASHBOARD, "Dashboard", Icons.Default.Dashboard),
    CampusQNavDestination(CampusQRoutes.ADMIN_MANAGE_SERVICES, "Services", Icons.Default.MiscellaneousServices),
    CampusQNavDestination(CampusQRoutes.ADMIN_STAFF_MANAGEMENT, "Staff", Icons.Default.Groups),
    CampusQNavDestination(CampusQRoutes.ADMIN_QUEUE_MONITORING, "Queues", Icons.AutoMirrored.Filled.FormatListBulleted),
    CampusQNavDestination(CampusQRoutes.ADMIN_REPORTS, "Reports", Icons.Default.Analytics)
)

val adminAllDrawerDestinations = listOf(
    CampusQNavDestination(CampusQRoutes.ADMIN_DASHBOARD, "Admin Dashboard", Icons.Default.Dashboard),
    CampusQNavDestination(CampusQRoutes.ADMIN_MANAGE_SERVICES, "Manage Services", Icons.Default.MiscellaneousServices),
    CampusQNavDestination(CampusQRoutes.ADMIN_STAFF_MANAGEMENT, "Staff Management", Icons.Default.Groups),
    CampusQNavDestination(CampusQRoutes.ADMIN_QUEUE_MONITORING, "Queue Monitoring", Icons.AutoMirrored.Filled.FormatListBulleted),
    CampusQNavDestination(CampusQRoutes.ADMIN_APPOINTMENTS, "Appointments", Icons.Default.CalendarMonth),
    CampusQNavDestination(CampusQRoutes.ADMIN_REPORTS, "Reports & Analytics", Icons.Default.Analytics),
    CampusQNavDestination(CampusQRoutes.ADMIN_SYSTEM_SETTINGS, "System Settings", Icons.Default.Settings)
)

@Composable
fun AdminScaffoldWithDrawer(
    title: String,
    currentRoute: String,
    headerColor: Color,
    onNavigate: (String) -> Unit,
    onExitToPreviewHub: () -> Unit,
    topBarActions: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                drawerContainerColor = CampusQSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CampusQAdminPurple)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_campusq_icon),
                                    contentDescription = "CampusQ Logo",
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        CampusQDemoLabel(isOnDarkHeader = true)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "CampusQ Admin",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Registrar System Administration",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                adminAllDrawerDestinations.forEach { dest ->
                    val selected = currentRoute == dest.route
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = dest.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        icon = {
                            Icon(imageVector = dest.icon, contentDescription = null)
                        },
                        selected = selected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigate(dest.route)
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = CampusQAdminPurpleLight,
                            selectedIconColor = CampusQAdminPurple,
                            selectedTextColor = CampusQAdminPurpleDark,
                            unselectedIconColor = CampusQTextSecondary,
                            unselectedTextColor = CampusQTextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = CampusQOutline,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("Switch Role Preview") },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onExitToPreviewHub()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Exit Admin Demo", color = CampusQError) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = CampusQError) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onExitToPreviewHub()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = CampusQBackground,
            topBar = {
                CampusQTopAppBar(
                    title = title,
                    containerColor = headerColor,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    actions = { topBarActions() }
                )
            },
            bottomBar = {
                val isPurpleScreen = headerColor == CampusQAdminPurple
                CampusQBottomNavigation(
                    destinations = adminBottomDestinations,
                    currentRoute = currentRoute,
                    onNavigate = onNavigate,
                    activeColor = if (isPurpleScreen) CampusQAdminPurple else CampusQGreenDark,
                    activeContainerColor = if (isPurpleScreen) CampusQAdminPurpleLight else CampusQGreenLight
                )
            },
            content = content
        )
    }
}

/**
 * SCREEN 17 — ADMIN DASHBOARD (Purple Header Accent per Reference)
 */
@Composable
fun AdminDashboardScreen(
    currentDateText: String,
    services: List<RegistrarService>,
    staffMembers: List<StaffProfile>,
    tickets: List<QueueTicket>,
    transactions: List<TransactionRecord>,
    onNavigate: (String) -> Unit,
    onExitToPreviewHub: () -> Unit
) {
    val activeStaffCount = staffMembers.count { it.isActive }
    val activeServiceCount = services.count { it.isAvailable }
    val waitingCount = tickets.count { it.status == QueueStatus.WAITING }
    val inServiceCount = tickets.count { it.status == QueueStatus.SERVING || it.status == QueueStatus.CALLED }
    val completedCount = transactions.count { it.queueStatus == QueueStatus.COMPLETED }
    val noShowCount = tickets.count { it.status == QueueStatus.NO_SHOW || it.status == QueueStatus.SKIPPED }

    AdminScaffoldWithDrawer(
        title = "Admin Dashboard",
        currentRoute = CampusQRoutes.ADMIN_DASHBOARD,
        headerColor = CampusQAdminPurple,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overview • $currentDateText",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = CampusQTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CampusQAdminPurpleLight
                ) {
                    Text(
                        text = "Demo Metrics",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CampusQAdminPurple,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Statistics: Total Users, Active Staff, In Service, Today's Transactions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CampusQStatCard(
                    title = "Total Users",
                    countValue = (tickets.map { it.studentId }.distinct().size + staffMembers.size).toString(),
                    accentColor = CampusQAdminPurple,
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                CampusQStatCard(
                    title = "Active Staff",
                    countValue = activeStaffCount.toString(),
                    accentColor = CampusQGreenPrimary,
                    icon = Icons.Default.Badge,
                    onClick = { onNavigate(CampusQRoutes.ADMIN_STAFF_MANAGEMENT) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CampusQStatCard(
                    title = "Active Services",
                    countValue = activeServiceCount.toString(),
                    accentColor = CampusQInfo,
                    icon = Icons.Default.MiscellaneousServices,
                    onClick = { onNavigate(CampusQRoutes.ADMIN_MANAGE_SERVICES) },
                    modifier = Modifier.weight(1f)
                )
                CampusQStatCard(
                    title = "Today's Transactions",
                    countValue = transactions.size.toString(),
                    accentColor = CampusQSuccess,
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    onClick = { onNavigate(CampusQRoutes.ADMIN_REPORTS) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            CampusQSectionHeading(
                title = "Queue Overview",
                actionText = "Monitor Queues",
                onActionClick = { onNavigate(CampusQRoutes.ADMIN_QUEUE_MONITORING) }
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CampusQStatCard(
                    title = "Waiting",
                    countValue = waitingCount.toString(),
                    accentColor = CampusQWarningDark,
                    icon = Icons.Default.HourglassEmpty,
                    onClick = { onNavigate(CampusQRoutes.ADMIN_QUEUE_MONITORING) },
                    modifier = Modifier.weight(1f)
                )
                CampusQStatCard(
                    title = "In Service",
                    countValue = inServiceCount.toString(),
                    accentColor = CampusQInfo,
                    icon = Icons.Default.Sync,
                    onClick = { onNavigate(CampusQRoutes.ADMIN_QUEUE_MONITORING) },
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
                    onClick = { onNavigate(CampusQRoutes.ADMIN_QUEUE_MONITORING) },
                    modifier = Modifier.weight(1f)
                )
                CampusQStatCard(
                    title = "No Show",
                    countValue = noShowCount.toString(),
                    accentColor = CampusQError,
                    icon = Icons.Default.PersonOff,
                    onClick = { onNavigate(CampusQRoutes.ADMIN_QUEUE_MONITORING) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            CampusQSectionHeading(title = "Administration Modules")
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CampusQSecondaryButton(
                    text = "Appointments",
                    leadingIcon = Icons.Default.CalendarMonth,
                    onClick = { onNavigate(CampusQRoutes.ADMIN_APPOINTMENTS) },
                    modifier = Modifier.weight(1f)
                )
                CampusQSecondaryButton(
                    text = "System Settings",
                    leadingIcon = Icons.Default.Settings,
                    onClick = { onNavigate(CampusQRoutes.ADMIN_SYSTEM_SETTINGS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * SCREEN 18 — MANAGE SERVICES (Green Header per Reference)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageServicesScreen(
    services: List<RegistrarService>,
    onNavigate: (String) -> Unit,
    onExitToPreviewHub: () -> Unit,
    onSaveService: (String?, String, String, String, ServiceIconType, Boolean, Boolean, Boolean, Int) -> Unit,
    onToggleServiceAvailability: (String) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showFormSheet by rememberSaveable { mutableStateOf(false) }
    var editingServiceId by rememberSaveable { mutableStateOf<String?>(null) }

    var formName by rememberSaveable { mutableStateOf("") }
    var formDescription by rememberSaveable { mutableStateOf("") }
    var formRequirements by rememberSaveable { mutableStateOf("") }
    var formDuration by rememberSaveable { mutableStateOf("10") }
    var formAvailable by rememberSaveable { mutableStateOf(true) }
    var formQueueEnabled by rememberSaveable { mutableStateOf(true) }
    var formAppointmentEnabled by rememberSaveable { mutableStateOf(true) }
    var formIconType by rememberSaveable { mutableStateOf(ServiceIconType.DOCUMENT_REQUEST) }
    var formNameError by rememberSaveable { mutableStateOf<String?>(null) }

    fun openAddForm() {
        editingServiceId = null
        formName = ""
        formDescription = ""
        formRequirements = "Valid Student ID, Request Form"
        formDuration = "10"
        formAvailable = true
        formQueueEnabled = true
        formAppointmentEnabled = true
        formIconType = ServiceIconType.DOCUMENT_REQUEST
        formNameError = null
        showFormSheet = true
    }

    fun openEditForm(srv: RegistrarService) {
        editingServiceId = srv.id
        formName = srv.name
        formDescription = srv.description
        formRequirements = srv.requirements.joinToString(", ")
        formDuration = srv.estimatedMinutes.toString()
        formAvailable = srv.isAvailable
        formQueueEnabled = srv.isQueueEnabled
        formAppointmentEnabled = srv.isAppointmentEnabled
        formIconType = srv.iconType
        formNameError = null
        showFormSheet = true
    }

    val filteredServices = services.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
    }

    AdminScaffoldWithDrawer(
        title = "Manage Services",
        currentRoute = CampusQRoutes.ADMIN_MANAGE_SERVICES,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registrar Services (${services.size})",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = { openAddForm() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CampusQGreenDark,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Service", style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            CampusQSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search configured services..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredServices, key = { it.id }) { service ->
                    var menuExpanded by remember { mutableStateOf(false) }

                    CampusQCard(
                        onClick = { openEditForm(service) },
                        contentPadding = 14.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (service.isAvailable) CampusQGreenLight else CampusQNeutralStatusBg,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = getServiceIcon(service.iconType),
                                        contentDescription = null,
                                        tint = if (service.isAvailable) CampusQGreenPrimary else CampusQNeutralStatus,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = service.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                CampusQStatusChip(
                                    label = if (service.isAvailable) "Active" else "Inactive",
                                    backgroundColor = if (service.isAvailable) CampusQSuccessBg else CampusQNeutralStatusBg,
                                    contentColor = if (service.isAvailable) CampusQSuccess else CampusQNeutralStatus
                                )
                            }

                            IconButton(onClick = { openEditForm(service) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Service",
                                    tint = CampusQGreenPrimary
                                )
                            }

                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = CampusQTextSecondary
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    modifier = Modifier.background(CampusQSurface)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Details") },
                                        onClick = {
                                            menuExpanded = false
                                            openEditForm(service)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(if (service.isAvailable) "Disable Service" else "Enable Service")
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onToggleServiceAvailability(service.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFormSheet = false },
            containerColor = CampusQSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (editingServiceId == null) "Add Registrar Service (Demo)" else "Edit Registrar Service (Demo)",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                CampusQTextField(
                    value = formName,
                    onValueChange = {
                        formName = it
                        formNameError = null
                    },
                    label = "Service Name",
                    placeholder = "e.g., Diploma Certified Copy",
                    errorMessage = formNameError
                )

                CampusQTextField(
                    value = formDescription,
                    onValueChange = { formDescription = it },
                    label = "Description",
                    placeholder = "Brief service description for students"
                )

                CampusQTextField(
                    value = formRequirements,
                    onValueChange = { formRequirements = it },
                    label = "Requirements (comma-separated)",
                    placeholder = "Valid Student ID, Clearance"
                )

                CampusQTextField(
                    value = formDuration,
                    onValueChange = { formDuration = it.filter { ch -> ch.isDigit() } },
                    label = "Estimated Duration (minutes)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Service Active / Available", style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary))
                    Switch(
                        checked = formAvailable,
                        onCheckedChange = { formAvailable = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CampusQGreenPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Walk-In Queue Enabled", style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary))
                    Switch(
                        checked = formQueueEnabled,
                        onCheckedChange = { formQueueEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CampusQGreenPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Appointment Booking Enabled", style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary))
                    Switch(
                        checked = formAppointmentEnabled,
                        onCheckedChange = { formAppointmentEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CampusQGreenPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CampusQPrimaryButton(
                    text = "Save Service (Demo)",
                    onClick = {
                        if (formName.isBlank()) {
                            formNameError = "Service name is required"
                        } else if ((formDuration.toIntOrNull() ?: 0) !in 1..480) {
                            formNameError = "Duration must be between 1 and 480 minutes."
                        } else {
                            onSaveService(
                                editingServiceId,
                                formName,
                                formDescription.ifBlank { "Registrar Office service" },
                                formRequirements,
                                formIconType,
                                formAvailable,
                                formQueueEnabled,
                                formAppointmentEnabled,
                                formDuration.toIntOrNull() ?: 10
                            )
                            showFormSheet = false
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * SCREEN 19 — STAFF MANAGEMENT (Green Header per Reference)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStaffManagementScreen(
    staffMembers: List<StaffProfile>,
    onNavigate: (String) -> Unit,
    onExitToPreviewHub: () -> Unit,
    onSaveStaff: (String?, String, String, String, Boolean) -> Unit,
    onToggleStaffActive: (String) -> Unit
) {
    var showStaffSheet by rememberSaveable { mutableStateOf(false) }
    var editingStaffId by rememberSaveable { mutableStateOf<String?>(null) }
    var formName by rememberSaveable { mutableStateOf("") }
    var formEmail by rememberSaveable { mutableStateOf("") }
    var formCounter by rememberSaveable { mutableStateOf("Window 1") }
    var formActive by rememberSaveable { mutableStateOf(true) }
    var formNameError by rememberSaveable { mutableStateOf<String?>(null) }

    var staffIdPendingDisable by rememberSaveable { mutableStateOf<String?>(null) }

    fun openAddStaff() {
        editingStaffId = null
        formName = ""
        formEmail = ""
        formCounter = "Window ${staffMembers.size + 1}"
        formActive = true
        formNameError = null
        showStaffSheet = true
    }

    fun openEditStaff(stf: StaffProfile) {
        editingStaffId = stf.id
        formName = stf.fullName
        formEmail = stf.email
        formCounter = stf.counterName
        formActive = stf.isActive
        formNameError = null
        showStaffSheet = true
    }

    AdminScaffoldWithDrawer(
        title = "Staff Management",
        currentRoute = CampusQRoutes.ADMIN_STAFF_MANAGEMENT,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registrar Personnel (${staffMembers.size})",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = { openAddStaff() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CampusQGreenDark,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Staff", style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(staffMembers, key = { it.id }) { staff ->
                    var menuExpanded by remember { mutableStateOf(false) }

                    CampusQCard(
                        onClick = { openEditStaff(staff) },
                        contentPadding = 14.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_avatar_staff),
                                contentDescription = "${staff.fullName} Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = staff.fullName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${staff.roleTitle} • ${staff.counterName}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CampusQTextSecondary)
                                )
                                if (LocalDensity.current.fontScale > 1.3f) {
                                    CampusQStatusChip(
                                        label = if (staff.isActive) "Active" else "Inactive",
                                        backgroundColor = if (staff.isActive) CampusQSuccessBg else CampusQNeutralStatusBg,
                                        contentColor = if (staff.isActive) CampusQSuccess else CampusQTextPrimary
                                    )
                                }
                            }

                            if (LocalDensity.current.fontScale <= 1.3f) CampusQStatusChip(
                                label = if (staff.isActive) "Active" else "Inactive",
                                backgroundColor = if (staff.isActive) CampusQSuccessBg else CampusQNeutralStatusBg,
                                contentColor = if (staff.isActive) CampusQSuccess else CampusQTextPrimary
                            )

                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Staff Options",
                                        tint = CampusQTextSecondary
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    modifier = Modifier.background(CampusQSurface)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Staff Details") },
                                        onClick = {
                                            menuExpanded = false
                                            openEditStaff(staff)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(if (staff.isActive) "Set Inactive" else "Set Active")
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            if (staff.isActive) {
                                                staffIdPendingDisable = staff.id
                                            } else {
                                                onToggleStaffActive(staff.id)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStaffSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStaffSheet = false },
            containerColor = CampusQSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (editingStaffId == null) "Add Registrar Staff (Demo)" else "Edit Registrar Staff (Demo)",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                CampusQTextField(
                    value = formName,
                    onValueChange = {
                        formName = it
                        formNameError = null
                    },
                    label = "Full Name",
                    placeholder = "Maria Santos",
                    errorMessage = formNameError
                )

                CampusQTextField(
                    value = formEmail,
                    onValueChange = { formEmail = it },
                    label = "Institutional Email",
                    placeholder = "staff@campusq.edu.ph"
                )

                CampusQTextField(
                    value = formCounter,
                    onValueChange = { formCounter = it },
                    label = "Assigned Counter / Window",
                    placeholder = "Window 1"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Role: Registrar Staff",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CampusQGreenDark, fontWeight = FontWeight.SemiBold)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Active", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = formActive,
                            onCheckedChange = { formActive = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CampusQGreenPrimary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                CampusQPrimaryButton(
                    text = "Save Staff Entry (Demo)",
                    onClick = {
                        if (formName.trim().length < 3) {
                            formNameError = "Enter a full staff name."
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(formEmail.trim()).matches()) {
                            formNameError = "Enter a valid institutional email."
                        } else if (formCounter.isBlank()) {
                            formNameError = "Assigned counter is required."
                        } else if (staffMembers.any { it.id != editingStaffId && it.email.equals(formEmail.trim(), true) }) {
                            formNameError = "A staff member already uses this email."
                        } else {
                            onSaveStaff(
                                editingStaffId,
                                formName,
                                formEmail.trim(),
                                formCounter,
                                formActive
                            )
                            showStaffSheet = false
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (staffIdPendingDisable != null) {
        val stf = staffMembers.find { it.id == staffIdPendingDisable }
        CampusQConfirmationDialog(
            title = "Disable Staff Profile?",
            message = "Mark ${stf?.fullName ?: "this staff member"} as Inactive in the local demonstration directory?",
            confirmText = "Set Inactive",
            isDestructive = true,
            onConfirm = {
                val id = staffIdPendingDisable!!
                staffIdPendingDisable = null
                onToggleStaffActive(id)
            },
            onDismiss = { staffIdPendingDisable = null }
        )
    }
}
