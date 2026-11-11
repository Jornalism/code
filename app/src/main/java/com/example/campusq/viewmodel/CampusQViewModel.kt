package com.example.campusq.viewmodel

import androidx.lifecycle.ViewModel
import com.example.campusq.data.demo.DemoRepository
import com.example.campusq.data.model.Appointment
import com.example.campusq.data.model.AppointmentStatus
import com.example.campusq.data.model.InstitutionSettings
import com.example.campusq.data.model.NotificationItem
import com.example.campusq.data.model.QueueStatus
import com.example.campusq.data.model.QueueTicket
import com.example.campusq.data.model.RegistrarService
import com.example.campusq.data.model.ServiceAnalyticsBar
import com.example.campusq.data.model.ServiceIconType
import com.example.campusq.data.model.StaffProfile
import com.example.campusq.data.model.StudentProfile
import com.example.campusq.data.model.TransactionRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CampusQViewModel(private val repository: DemoRepository = DemoRepository()) : ViewModel() {

    val studentProfile: StateFlow<StudentProfile> = repository.studentProfile
    val currentStaffProfile: StateFlow<StaffProfile> = repository.currentStaffProfile
    val services: StateFlow<List<RegistrarService>> = repository.services
    val staffMembers: StateFlow<List<StaffProfile>> = repository.staffMembers
    val queueTickets: StateFlow<List<QueueTicket>> = repository.queueTickets
    val studentTicketId: StateFlow<String?> = repository.studentTicketId
    val appointments: StateFlow<List<Appointment>> = repository.appointments
    val transactions: StateFlow<List<TransactionRecord>> = repository.transactions
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
    val institutionSettings: StateFlow<InstitutionSettings> = repository.institutionSettings

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _selectedServiceForBooking = MutableStateFlow<RegistrarService?>(null)
    val selectedServiceForBooking: StateFlow<RegistrarService?> = _selectedServiceForBooking.asStateFlow()

    fun currentFormattedDate(): String = repository.currentFormattedDate()

    fun showMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }

    fun selectServiceForBooking(service: RegistrarService?) {
        _selectedServiceForBooking.value = service
    }

    fun joinQueue(service: RegistrarService): QueueTicket? = attempt {
        val ticket = repository.joinQueueForStudent(service)
        showMessage("Demo ticket ${ticket.ticketNumber} created for ${service.name}.")
        ticket
    }

    fun cancelStudentTicket(ticketId: String) {
        attempt {
        repository.cancelStudentActiveTicket(ticketId)
        showMessage("Demo ticket cancelled.")

        }
    }

    fun restoreDemoTicketR001() {
        attempt {
        repository.restoreDemoTicketR001()
        showMessage("Demo ticket R-001 restored to WAITING.")

        }
    }

    fun bookAppointment(service: RegistrarService, dateText: String, timeSlot: String): Appointment? = attempt {
        val apt = repository.bookStudentAppointment(service, dateText, timeSlot)
        showMessage("Demo appointment ${apt.referenceNumber} added.")
        apt
    }

    fun updateTicketStatus(ticketId: String, newStatus: QueueStatus) {
        attempt {
        repository.updateTicketStatus(ticketId, newStatus)
        val actionLabel = when (newStatus) {
            QueueStatus.CALLED -> "Demo ticket called."
            QueueStatus.SERVING -> "Demo service started."
            QueueStatus.COMPLETED -> "Demo service completed."
            QueueStatus.SKIPPED -> "Demo ticket skipped."
            QueueStatus.WAITING -> "Demo ticket recalled to waiting."
            QueueStatus.CANCELLED -> "Demo ticket cancelled."
            QueueStatus.NO_SHOW -> "Demo ticket marked as No Show."
            QueueStatus.EXPIRED -> "Demo ticket marked as Expired."
        }
        showMessage(actionLabel)

        }
    }

    fun callNextWaitingTicket(): QueueTicket? {
        val called = repository.callNextWaitingTicket()
        if (called != null) {
            showMessage("Demo ticket ${called.ticketNumber} called.")
        } else {
            showMessage("No waiting demo tickets in line.")
        }
        return called
    }

    fun updateAppointmentStatus(appointmentId: String, newStatus: AppointmentStatus) {
        attempt {
        repository.updateAppointmentStatus(appointmentId, newStatus)
        showMessage("Demo appointment status updated to ${newStatus.displayName}.")

        }
    }

    fun saveService(
        id: String?,
        name: String,
        description: String,
        requirementsText: String,
        iconType: ServiceIconType,
        isAvailable: Boolean,
        isQueueEnabled: Boolean,
        isAppointmentEnabled: Boolean,
        estimatedMinutes: Int
    ) {
        attempt {
        repository.saveService(
            id = id,
            name = name,
            description = description,
            requirementsText = requirementsText,
            iconType = iconType,
            isAvailable = isAvailable,
            isQueueEnabled = isQueueEnabled,
            isAppointmentEnabled = isAppointmentEnabled,
            estimatedMinutes = estimatedMinutes
        )
        showMessage(if (id == null) "Demo service added." else "Demo service updated.")

        }
    }

    fun toggleServiceAvailability(serviceId: String) {
        attempt {
        repository.toggleServiceAvailability(serviceId)
        showMessage("Demo service availability updated.")

        }
    }

    fun saveStaffMember(
        id: String?,
        fullName: String,
        email: String,
        counterName: String,
        isActive: Boolean
    ) {
        attempt {
        repository.saveStaffMember(id, fullName, email, counterName, isActive)
        showMessage(if (id == null) "Demo staff member added." else "Demo staff member updated.")

        }
    }

    fun toggleStaffActive(staffId: String) {
        attempt {
        repository.toggleStaffActive(staffId)
        showMessage("Demo staff status updated.")

        }
    }

    fun updateStudentProfile(fullName: String, studentId: String, email: String) {
        attempt {
        repository.updateStudentProfile(fullName, studentId, email)
        showMessage("Demo student profile updated locally.")

        }
    }

    fun updateInstitutionSettings(settings: InstitutionSettings) {
        attempt {
        repository.updateInstitutionSettings(settings)
        showMessage("Demo system settings updated locally.")

        }
    }

    fun markAllNotificationsRead() {
        repository.markAllNotificationsRead()
        showMessage("All demo notifications marked as read.")
    }

    fun rescheduleAppointment(id: String, service: RegistrarService, dateText: String, timeSlot: String): Boolean =
        attempt {
            repository.rescheduleStudentAppointment(id, service, dateText, timeSlot)
            showMessage("Demo appointment rescheduled.")
            true
        } ?: false

    fun cancelAppointment(id: String) {
        attempt {
            repository.cancelStudentAppointment(id)
            showMessage("Demo appointment cancelled.")
        }
    }

    private fun <T> attempt(action: () -> T): T? = try {
        action()
    } catch (error: IllegalArgumentException) {
        showMessage(error.message ?: "This action is unavailable.")
        null
    }

    fun getAnalyticsByService(periodIndex: Int): List<ServiceAnalyticsBar> {
        return repository.getAnalyticsByService(periodIndex)
    }
}
