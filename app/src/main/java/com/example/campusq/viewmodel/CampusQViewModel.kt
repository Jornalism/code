package com.example.campusq.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.campusq.data.local.LocalCampusQRepository
import com.example.campusq.data.local.LocalApiException
import com.example.campusq.data.local.jsonBody
import com.example.campusq.ui.navigation.CampusQRoutes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.util.UUID
import androidx.lifecycle.ViewModel
import com.example.campusq.data.repository.CampusQStateSource
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

class CampusQViewModel(repository: DemoRepository? = null, private val remote: LocalCampusQRepository? = null) : ViewModel() {
    private val demo by lazy { repository ?: DemoRepository() }
    private val state: CampusQStateSource = remote ?: demo
    val isLive: Boolean get() = remote != null
    val networkBusy = MutableStateFlow(false)
    val connectionIssue = MutableStateFlow<String?>(null)
    val liveReady = MutableStateFlow(false)
    val liveSessionExpired = MutableStateFlow(false)
    val navigationEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    private val remoteLock = Mutex()
    private val pendingRequestKeys = mutableMapOf<String, String>()

    suspend fun synchronizeLive() {
        if (remote == null) return
        remoteLock.withLock {
            try { remote.refresh(); liveReady.value = true; connectionIssue.value = null }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: LocalApiException) {
                if (error.status == 401) liveSessionExpired.value = true
                connectionIssue.value = error.message
            } catch (_: Exception) { connectionIssue.value = "Cannot reach the laptop server. Displayed information may be stale. Changes require a server connection." }
        }
    }

    private fun sendLive(method: String, path: String, body: JSONObject = jsonBody(), destination: String? = null, idempotent: Boolean = false) {
        val backend = remote ?: return
        if (networkBusy.value) return
        networkBusy.value = true
        val fingerprint = if (idempotent) path + body.toString() else ""
        val key = if (idempotent) pendingRequestKeys.getOrPut(fingerprint) { UUID.randomUUID().toString() } else null
        viewModelScope.launch {
            try {
                remoteLock.withLock {
                    backend.api.request(method, path, body, key)
                    backend.refresh()
                    liveReady.value = true
                    connectionIssue.value = null
                    if (idempotent) pendingRequestKeys.remove(fingerprint)
                }
                if (path.startsWith("/api/services") || path.startsWith("/api/staff") || path == "/api/profile" || path == "/api/settings") {
                    showMessage("Saved on the CampusQ server.")
                }
                destination?.let { navigationEvents.emit(it) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: LocalApiException) {
                if (error.status == 401) liveSessionExpired.value = true
                if (idempotent && error.status < 500) pendingRequestKeys.remove(fingerprint)
                showMessage(error.message)
            } catch (_: Exception) {
                connectionIssue.value = "The request result could not be confirmed. Check the server and retry; queue and booking retries reuse the same request key."
                showMessage(requireNotNull(connectionIssue.value))
            } finally { networkBusy.value = false }
        }
    }

    fun adminOverview(): String = remote?.adminOverview ?: "Available only for a local-server administrator."
    private fun appointmentVersion(id: String) = appointments.value.find { it.id == id }?.version ?: 0
    private fun ticketVersion(id: String) = queueTickets.value.find { it.id == id }?.version ?: 0


    val studentProfile: StateFlow<StudentProfile> = state.studentProfile
    val currentStaffProfile: StateFlow<StaffProfile> = state.currentStaffProfile
    val services: StateFlow<List<RegistrarService>> = state.services
    val staffMembers: StateFlow<List<StaffProfile>> = state.staffMembers
    val queueTickets: StateFlow<List<QueueTicket>> = state.queueTickets
    val studentTicketId: StateFlow<String?> = state.studentTicketId
    val nowServing: StateFlow<Map<String, String>> = remote?.nowServing ?: MutableStateFlow(emptyMap())
    val reservedSlots: StateFlow<List<com.example.campusq.data.model.AppointmentSlot>> = remote?.reservedSlots ?: MutableStateFlow(emptyList())
    val appointments: StateFlow<List<Appointment>> = state.appointments
    val transactions: StateFlow<List<TransactionRecord>> = state.transactions
    val notifications: StateFlow<List<NotificationItem>> = state.notifications
    val institutionSettings: StateFlow<InstitutionSettings> = state.institutionSettings

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _selectedServiceForBooking = MutableStateFlow<RegistrarService?>(null)
    val selectedServiceForBooking: StateFlow<RegistrarService?> = _selectedServiceForBooking.asStateFlow()

    fun currentFormattedDate(): String = if (isLive) java.time.LocalDate.now().format(com.example.campusq.data.model.DemoBookingRules.dateFormatter) else demo.currentFormattedDate()

    fun showMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }

    fun selectServiceForBooking(service: RegistrarService?) {
        _selectedServiceForBooking.value = service
    }

    fun joinQueue(service: RegistrarService): QueueTicket? {
        if (isLive) {
            sendLive("POST", "/api/tickets", jsonBody("serviceId" to service.id), CampusQRoutes.STUDENT_QUEUE_STATUS, true)
            return null
        }
        return attempt {
        val ticket = demo.joinQueueForStudent(service)
        showMessage("Demo ticket ${ticket.ticketNumber} created for ${service.name}.")
        ticket
        }
    }

    fun cancelStudentTicket(ticketId: String) {
        if (isLive) { sendLive("PATCH", "/api/tickets/$ticketId", jsonBody("status" to "CANCELLED", "version" to ticketVersion(ticketId))); return }
        attempt {
        demo.cancelStudentActiveTicket(ticketId)
        showMessage("Demo ticket cancelled.")

        }
    }

    fun restoreDemoTicketR001() {
        if (isLive) { showMessage("Request a new ticket from Registrar Services."); return }
        attempt {
        demo.restoreDemoTicketR001()
        showMessage("Demo ticket R-001 restored to WAITING.")

        }
    }

    fun bookAppointment(service: RegistrarService, dateText: String, timeSlot: String): Appointment? {
        if (isLive) {
            sendLive("POST", "/api/appointments", jsonBody("serviceId" to service.id, "dateText" to dateText, "timeSlot" to timeSlot), CampusQRoutes.STUDENT_BOOK_APPOINTMENT, true)
            return null
        }
        return attempt {
        val apt = demo.bookStudentAppointment(service, dateText, timeSlot)
        showMessage("Demo appointment ${apt.referenceNumber} added.")
        apt
        }
    }

    fun updateTicketStatus(ticketId: String, newStatus: QueueStatus, destination: String? = null) {
        if (isLive) { sendLive("PATCH", "/api/tickets/$ticketId", jsonBody("status" to newStatus.name, "version" to ticketVersion(ticketId)), destination ?: if (newStatus == QueueStatus.SERVING) CampusQRoutes.staffServiceInProgressRoute(ticketId) else null); return }
        attempt {
        demo.updateTicketStatus(ticketId, newStatus)
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
        if (isLive) { sendLive("POST", "/api/tickets/next"); return null }
        val called = demo.callNextWaitingTicket()
        if (called != null) {
            showMessage("Demo ticket ${called.ticketNumber} called.")
        } else {
            showMessage("No waiting demo tickets in line.")
        }
        return called
    }

    fun updateAppointmentStatus(appointmentId: String, newStatus: AppointmentStatus) {
        if (isLive) { sendLive("PATCH", "/api/appointments/$appointmentId", jsonBody("status" to newStatus.name, "version" to appointmentVersion(appointmentId))); return }
        attempt {
        demo.updateAppointmentStatus(appointmentId, newStatus)
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
        if (isLive) {
            val body = jsonBody("name" to name, "description" to description, "requirementsText" to requirementsText, "iconType" to iconType.name,
                "isAvailable" to isAvailable, "isQueueEnabled" to isQueueEnabled, "isAppointmentEnabled" to isAppointmentEnabled,
                "estimatedMinutes" to estimatedMinutes)
            if (id != null) body.put("version", services.value.find { it.id == id }?.version ?: 0)
            sendLive(if (id == null) "POST" else "PATCH", if (id == null) "/api/services" else "/api/services/$id", body)
            return
        }
        attempt {
        demo.saveService(
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

    fun deleteUnusedService(serviceId: String) {
        if (isLive) sendLive("DELETE", "/api/services/$serviceId")
        else showMessage("Delete is available only for unused server services.")
    }

    fun toggleServiceAvailability(serviceId: String) {
        if (isLive) {
            val service = services.value.find { it.id == serviceId } ?: return
            saveService(service.id, service.name, service.description, service.requirements.joinToString("\n"), service.iconType,
                !service.isAvailable, service.isQueueEnabled, service.isAppointmentEnabled, service.estimatedMinutes)
            return
        }
        attempt {
        demo.toggleServiceAvailability(serviceId)
        showMessage("Demo service availability updated.")

        }
    }

    fun saveStaffMember(
        id: String?,
        fullName: String,
        email: String,
        counterName: String,
        isActive: Boolean,
        initialPassword: String? = null
    ) {
        if (isLive) {
            val body = jsonBody("fullName" to fullName, "email" to email, "counterName" to counterName, "isActive" to isActive)
            if (id == null) {
                if (initialPassword.isNullOrBlank()) { showMessage("Enter an initial staff password."); return }
                body.put("password", initialPassword)
            }
            sendLive(if (id == null) "POST" else "PATCH", if (id == null) "/api/staff" else "/api/staff/$id", body)
            return
        }
        attempt {
        demo.saveStaffMember(id, fullName, email, counterName, isActive)
        showMessage(if (id == null) "Demo staff member added." else "Demo staff member updated.")

        }
    }

    fun toggleStaffActive(staffId: String) {
        if (isLive) {
            val staff = staffMembers.value.find { it.id == staffId } ?: return
            saveStaffMember(staff.id, staff.fullName, staff.email, staff.counterName, !staff.isActive)
            return
        }
        attempt {
        demo.toggleStaffActive(staffId)
        showMessage("Demo staff status updated.")

        }
    }

    fun updateStudentProfile(fullName: String, studentId: String, email: String) {
        if (isLive) { sendLive("PATCH", "/api/profile", jsonBody("fullName" to fullName, "studentId" to studentId, "email" to email)); return }
        attempt {
        demo.updateStudentProfile(fullName, studentId, email)
        showMessage("Demo student profile updated locally.")

        }
    }

    fun updateInstitutionSettings(settings: InstitutionSettings) {
        if (isLive) {
            sendLive("PATCH", "/api/settings", jsonBody("institutionName" to settings.institutionName, "operatingDays" to settings.operatingDays,
                "operatingHours" to settings.operatingHours, "maxDailyQueueLimit" to settings.maxDailyQueueLimit,
                "pushAlertsEnabled" to settings.pushAlertsEnabled, "smsFallbackEnabled" to settings.smsFallbackEnabled))
            return
        }
        attempt {
        demo.updateInstitutionSettings(settings)
        showMessage("Demo system settings updated locally.")

        }
    }

    fun markAllNotificationsRead() {
        if (isLive) { sendLive("POST", "/api/notifications/read"); return }
        demo.markAllNotificationsRead()
        showMessage("All demo notifications marked as read.")
    }

    fun rescheduleAppointment(id: String, service: RegistrarService, dateText: String, timeSlot: String): Boolean {
        if (isLive) {
            sendLive("PATCH", "/api/appointments/$id", jsonBody("serviceId" to service.id, "dateText" to dateText, "timeSlot" to timeSlot, "version" to appointmentVersion(id)), CampusQRoutes.STUDENT_BOOK_APPOINTMENT)
            return false
        }
        return attempt {
            demo.rescheduleStudentAppointment(id, service, dateText, timeSlot)
            showMessage("Demo appointment rescheduled.")
            true
        } ?: false
    }

    fun cancelAppointment(id: String) {
        if (isLive) { updateAppointmentStatus(id, AppointmentStatus.CANCELLED); return }
        attempt {
            demo.cancelStudentAppointment(id)
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
        return remote?.analytics?.getOrNull(periodIndex) ?: demo.getAnalyticsByService(periodIndex)
    }
}
