package com.example.campusq.ui.components

import com.example.campusq.ui.navigation.LocalServerMode
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusq.ui.theme.CampusQAdminPurple
import com.example.campusq.ui.theme.CampusQAdminPurpleLight
import com.example.campusq.ui.theme.CampusQError
import com.example.campusq.ui.theme.CampusQGreenDark
import com.example.campusq.ui.theme.CampusQGreenLight
import com.example.campusq.ui.theme.CampusQGreenPrimary
import com.example.campusq.ui.theme.CampusQOutline
import com.example.campusq.ui.theme.CampusQSurface
import com.example.campusq.ui.theme.CampusQSurfaceVariant
import com.example.campusq.ui.theme.CampusQTextMuted
import com.example.campusq.ui.theme.CampusQTextPrimary
import com.example.campusq.ui.theme.CampusQTextSecondary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CampusQNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusQTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    containerColor: Color = CampusQGreenPrimary,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    showDemoBadge: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val headerColor = if (containerColor == CampusQGreenPrimary) CampusQGreenDark else containerColor
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                )
                if (showDemoBadge) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CampusQDemoLabel(isOnDarkHeader = true)
                }
            }
        },
        navigationIcon = {
            when {
                onBackClick != null -> {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
                onMenuClick != null -> {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Menu",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = headerColor,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = modifier
    )
}

@Composable
fun CampusQBottomNavigation(
    destinations: List<CampusQNavDestination>,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = CampusQGreenDark,
    activeContainerColor: Color = CampusQGreenLight
) {
    NavigationBar(
        modifier = modifier,
        containerColor = CampusQSurface,
        tonalElevation = 4.dp
    ) {
        destinations.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(dest.route) },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = if (LocalDensity.current.fontScale > 1.3f) {
                            when (dest.label) { "Appointments" -> "Appts"; "Dashboard" -> "Home"; else -> dest.label }
                        } else dest.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    indicatorColor = activeContainerColor,
                    unselectedIconColor = CampusQTextSecondary,
                    unselectedTextColor = CampusQTextPrimary
                )
            )
        }
    }
}

@Composable
fun CampusQDemoLabel(
    modifier: Modifier = Modifier,
    isOnDarkHeader: Boolean = false
) {
    if (LocalServerMode.current) return

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (isOnDarkHeader) Color.White else CampusQGreenLight
    ) {
        Text(
            text = "DEMO",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CampusQGreenDark
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CampusQSectionHeading(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp)
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = CampusQGreenDark,
                    fontSize = 13.sp
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onActionClick)
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun CampusQFilterTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = CampusQGreenPrimary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val selected = selectedIndex == index
            Surface(
                shape = RoundedCornerShape(50),
                color = if (selected) activeColor else CampusQSurface,
                border = if (selected) null else BorderStroke(1.dp, CampusQOutline),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .heightIn(min = 48.dp)
                    .selectable(selected = selected, role = Role.Tab) { onTabSelected(index) }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        color = if (selected) Color.White else CampusQTextSecondary,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun CampusQQueueProgressIndicator(
    peopleAhead: Int,
    modifier: Modifier = Modifier,
    label: String = "People ahead",
    ringSize: Dp = 156.dp
) {
    val progress = when {
        peopleAhead <= 0 -> 1f
        peopleAhead >= 10 -> 0.15f
        else -> (1f - (peopleAhead / 10f)).coerceIn(0.2f, 0.9f)
    }

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val strokeWidth = 12.dp.toPx()
            drawArc(
                color = CampusQGreenLight,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = CampusQGreenPrimary,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = peopleAhead.toString(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CampusQGreenPrimary
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CampusQTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun CampusQProfileHeader(
    fullName: String,
    subtitle1: String,
    modifier: Modifier = Modifier,
    subtitle2: String? = null,
    avatarResId: Int? = null,
    initials: String = "CQ",
    accentColor: Color = CampusQGreenDark
) {
    CampusQCard(
        modifier = modifier,
        contentPadding = 20.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (avatarResId != null) {
                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = "$fullName Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(88.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = fullName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CampusQTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle1,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            )
            if (subtitle2 != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle2,
                    style = MaterialTheme.typography.bodySmall.copy(color = CampusQTextSecondary)
                )
            }
        }
    }
}

@Composable
fun CampusQProfileMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isDestructive: Boolean = false,
    accentColor: Color = CampusQGreenDark
) {
    val itemColor = if (isDestructive) CampusQError else CampusQTextPrimary
    val iconTint = if (isDestructive) CampusQError else accentColor
    val iconBg = if (isDestructive) CampusQError.copy(alpha = 0.1f) else accentColor.copy(alpha = 0.12f)

    CampusQCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconBg,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = itemColor,
                        fontSize = 15.sp
                    )
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(color = CampusQTextSecondary)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isDestructive) CampusQError else CampusQTextSecondary
            )
        }
    }
}

@Composable
fun CampusQEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    CampusQCard(
        modifier = modifier,
        contentPadding = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = CampusQGreenLight,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CampusQGreenPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            if (actionButtonText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                CampusQPrimaryButton(
                    text = actionButtonText,
                    onClick = onActionClick
                )
            }
        }
    }
}

@Composable
fun CampusQConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (isDestructive) CampusQError else CampusQGreenPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = CampusQTextSecondary
                )
            }
        },
        containerColor = CampusQSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CampusQTimeSlotChip(
    timeSlot: String,
    isSelected: Boolean,
    isAvailable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        !isAvailable -> CampusQSurfaceVariant
        isSelected -> CampusQGreenDark
        else -> CampusQGreenLight
    }
    val textColor = when {
        !isAvailable -> CampusQTextMuted
        isSelected -> Color.White
        else -> CampusQGreenDark
    }

    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .selectable(selected = isSelected, enabled = isAvailable, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = if (!isAvailable) BorderStroke(1.dp, CampusQOutline) else null
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = timeSlot,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 13.sp,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun CampusQDateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val scroll = rememberScrollState()
    val firstOffset = currentMonth.atDay(1).dayOfWeek.value % 7
    val totalCells = ((firstOffset + currentMonth.lengthOfMonth() + 6) / 7) * 7
    CampusQCard(modifier = modifier, contentPadding = 14.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousMonth, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.ChevronLeft, "Previous Month", tint = CampusQGreenPrimary)
            }
            Text(
                currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)),
                modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                maxLines = 2, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onNextMonth, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.ChevronRight, "Next Month", tint = CampusQGreenPrimary)
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(modifier = Modifier.horizontalScroll(scroll)) {
            Column(modifier = Modifier.width(336.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                for (row in 0 until totalCells / 7) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (column in 0 until 7) {
                            val day = row * 7 + column - firstOffset + 1
                            if (day in 1..currentMonth.lengthOfMonth()) {
                                val date = currentMonth.atDay(day)
                                val available = column != 0 && column != 6 && !date.isBefore(today)
                                val selected = date == selectedDate
                                Box(
                                    modifier = Modifier.weight(1f).height(52.dp)
                                        .semantics { contentDescription = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)) }
                                        .selectable(selected = selected, enabled = available, role = Role.Button) { onDateSelected(date) }
                                        .padding(2.dp).clip(CircleShape)
                                        .background(when {
                                            selected -> CampusQGreenDark
                                            date == today -> CampusQGreenLight
                                            else -> Color.Transparent
                                        }),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        day.toString(),
                                        modifier = Modifier.clearAndSetSemantics {},
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            fontWeight = if (selected || date == today) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                selected -> Color.White
                                                !available -> CampusQTextSecondary
                                                date == today -> CampusQGreenDark
                                                else -> CampusQTextPrimary
                                            }
                                        )
                                    )
                                }
                            } else Spacer(modifier = Modifier.weight(1f).height(52.dp))
                        }
                    }
                }
            }
        }
        if (scroll.maxValue > with(LocalDensity.current) { 8.dp.toPx() }) {
            Text("Swipe calendar to see all days", style = MaterialTheme.typography.bodySmall)
        }
    }
}
