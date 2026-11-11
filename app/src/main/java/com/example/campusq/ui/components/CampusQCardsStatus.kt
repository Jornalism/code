package com.example.campusq.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusq.data.model.Appointment
import com.example.campusq.data.model.AppointmentStatus
import com.example.campusq.data.model.QueueStatus
import com.example.campusq.data.model.QueueTicket
import com.example.campusq.data.model.RegistrarService
import com.example.campusq.data.model.ServiceIconType
import com.example.campusq.ui.theme.CampusQDimens
import com.example.campusq.ui.theme.CampusQError
import com.example.campusq.ui.theme.CampusQErrorText
import com.example.campusq.ui.theme.CampusQErrorBg
import com.example.campusq.ui.theme.CampusQGreenDark
import com.example.campusq.ui.theme.CampusQGreenLight
import com.example.campusq.ui.theme.CampusQGreenPrimary
import com.example.campusq.ui.theme.CampusQInfo
import com.example.campusq.ui.theme.CampusQInfoBg
import com.example.campusq.ui.theme.CampusQNeutralStatus
import com.example.campusq.ui.theme.CampusQNeutralStatusBg
import com.example.campusq.ui.theme.CampusQOutline
import com.example.campusq.ui.theme.CampusQSuccess
import com.example.campusq.ui.theme.CampusQSuccessBg
import com.example.campusq.ui.theme.CampusQSurface
import com.example.campusq.ui.theme.CampusQTextPrimary
import com.example.campusq.ui.theme.CampusQTextSecondary
import com.example.campusq.ui.theme.CampusQWarning
import com.example.campusq.ui.theme.CampusQWarningBg
import com.example.campusq.ui.theme.CampusQWarningDark

@Composable
fun CampusQCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CampusQDimens.CardRadius,
    containerColor: Color = CampusQSurface,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(0.8.dp, CampusQOutline.copy(alpha = 0.65f))
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
fun CampusQStatusChip(
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(contentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                maxLines = 1,
                softWrap = false,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            )
        }
    }
}

@Composable
fun CampusQQueueStatusChip(
    status: QueueStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (status) {
        QueueStatus.WAITING -> CampusQWarningBg to CampusQWarningDark
        QueueStatus.CALLED -> CampusQInfoBg to CampusQInfo
        QueueStatus.SERVING -> CampusQInfoBg to CampusQInfo
        QueueStatus.COMPLETED -> CampusQSuccessBg to CampusQSuccess
        QueueStatus.SKIPPED -> CampusQWarningBg to CampusQWarningDark
        QueueStatus.NO_SHOW -> CampusQErrorBg to CampusQErrorText
        QueueStatus.CANCELLED -> CampusQErrorBg to CampusQErrorText
        QueueStatus.EXPIRED -> CampusQNeutralStatusBg to CampusQTextPrimary
    }
    CampusQStatusChip(
        label = status.displayName,
        backgroundColor = bg,
        contentColor = fg,
        modifier = modifier
    )
}

@Composable
fun CampusQAppointmentStatusChip(
    status: AppointmentStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (status) {
        AppointmentStatus.BOOKED -> CampusQWarningBg to CampusQWarningDark
        AppointmentStatus.CONFIRMED -> CampusQGreenLight to CampusQGreenDark
        AppointmentStatus.CHECKED_IN -> CampusQInfoBg to CampusQInfo
        AppointmentStatus.SERVING -> CampusQInfoBg to CampusQInfo
        AppointmentStatus.COMPLETED -> CampusQSuccessBg to CampusQSuccess
        AppointmentStatus.CANCELLED -> CampusQErrorBg to CampusQErrorText
        AppointmentStatus.NO_SHOW -> CampusQErrorBg to CampusQErrorText
    }
    CampusQStatusChip(
        label = status.displayName,
        backgroundColor = bg,
        contentColor = fg,
        modifier = modifier
    )
}

@Composable
fun CampusQTicketCard(
    ticket: QueueTicket,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CampusQCard(
        modifier = modifier,
        cornerRadius = CampusQDimens.FeatureCardRadius,
        onClick = onClick,
        contentPadding = 18.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Queue Ticket",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(color = CampusQTextSecondary)
            )
            CampusQQueueStatusChip(status = ticket.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ticket.ticketNumber,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CampusQGreenPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ticket.serviceName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = CampusQTextPrimary
                    )
                )
            }
            Surface(
                shape = CircleShape,
                color = CampusQGreenLight,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Queue Status",
                        tint = CampusQGreenPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = CampusQOutline.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = CampusQGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${ticket.peopleAhead} people ahead",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CampusQTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = CampusQWarning,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Est. wait: ${ticket.estimatedWaitMinutes} min",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CampusQTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

fun getServiceIcon(iconType: ServiceIconType): ImageVector = when (iconType) {
    ServiceIconType.DOCUMENT_REQUEST -> Icons.Default.Description
    ServiceIconType.ENROLLMENT_VERIFICATION -> Icons.Default.HowToReg
    ServiceIconType.GOOD_MORAL -> Icons.Default.VerifiedUser
    ServiceIconType.TRANSCRIPT -> Icons.Default.School
    ServiceIconType.OTHER -> Icons.Default.FolderSpecial
}

@Composable
fun CampusQServiceCard(
    service: RegistrarService,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                shape = RoundedCornerShape(12.dp),
                color = if (service.isAvailable) CampusQGreenLight else CampusQNeutralStatusBg,
                modifier = Modifier.size(48.dp)
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

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (service.isAvailable) CampusQTextPrimary else CampusQTextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!service.isAvailable) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CampusQStatusChip(
                            label = "UNAVAILABLE",
                            backgroundColor = CampusQNeutralStatusBg,
                            contentColor = CampusQTextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Service Options",
                tint = CampusQTextSecondary
            )
        }
    }
}

@Composable
fun CampusQAppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionSlot: (@Composable () -> Unit)? = null
) {
    val largeText = LocalDensity.current.fontScale > 1.3f
    CampusQCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CampusQGreenLight,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = CampusQGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${appointment.referenceNumber} — ${appointment.serviceName}",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = if (largeText) 3 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = appointment.studentName,
                        style = MaterialTheme.typography.bodySmall.copy(color = CampusQTextSecondary)
                    )
                    if (largeText) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CampusQAppointmentStatusChip(appointment.status)
                    }
                }
            }
            if (!largeText) CampusQAppointmentStatusChip(status = appointment.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (largeText) {
            Text(
                text = "${appointment.dateText} • ${appointment.timeSlot}",
                style = MaterialTheme.typography.bodySmall
            )
            actionSlot?.invoke()
        } else Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = CampusQTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${appointment.dateText} • ${appointment.timeSlot}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CampusQTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            if (actionSlot != null) {
                actionSlot()
            }
        }
    }
}

@Composable
fun CampusQStatCard(
    title: String,
    countValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = CampusQSurface,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    CampusQCard(
        modifier = modifier,
        containerColor = backgroundColor,
        onClick = onClick,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = countValue,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CampusQTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            if (icon != null) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.14f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
