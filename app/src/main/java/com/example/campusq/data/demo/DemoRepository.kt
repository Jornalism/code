package com.example.campusq.data.demo

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
import com.example.campusq.data.model.TransactionType
import com.example.campusq.data.repository.CampusQStateSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.campusq.data.model.DemoBookingRules
import com.example.campusq.data.model.canTransitionTo
import com.example.campusq.data.model.canEdit
import com.example.campusq.data.model.isActive
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class DemoRepository(private val clock: java.time.Clock = java.time.Clock.systemDefaultZone()) : CampusQStateSource {

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US)
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    fun currentFormattedDate(): String = LocalDate.now(clock).format(dateFormatter)
    fun currentFormattedTime(): String = LocalTime.now(clock).format(timeFormatter)

    private val _studentProfile = MutableStateFlow(
        StudentProfile(
            id = "stu-001",
            fullName = "Juan Dela Cruz",
            studentId = "2023-10482",
            email = "juan.delacruz@campusq.edu.ph",
            program = "BS in Information Technology",
            yearLevel = "4th Year"
        )
    )
    override val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    private val _currentStaffProfile = MutableStateFlow(
        StaffProfile(
            id = "stf-001",
            fullName = "Maria Santos",
            email = "maria.santos@campusq.edu.ph",
            roleTitle = "Registrar Staff",
            counterName = "Window 1",
            isActive = true
        )
    )
    override val currentStaffProfile: StateFlow<StaffProfile> = _currentStaffProfile.asStateFlow()

    private val _services = MutableStateFlow(initialServices())
    override val services: StateFlow<List<RegistrarService>> = _services.asStateFlow()

    private val _staffMembers = MutableStateFlow(initialStaffMembers())
    override val staffMembers: StateFlow<List<StaffProfile>> = _staffMembers.asStateFlow()

    private val _queueTickets = MutableStateFlow(initialQueueTickets())
    override val queueTickets: StateFlow<List<QueueTicket>> = _queueTickets.asStateFlow()

    private val _studentTicketId = MutableStateFlow<String?>("tkt-r001")
    override val studentTicketId: StateFlow<String?> = _studentTicketId.asStateFlow()

    private val _appointments = MutableStateFlow(initialAppointments())
    override val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _transactions = MutableStateFlow(initialTransactions())
    override val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    private val _notifications = MutableStateFlow(initialNotifications())
    override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _institutionSettings = MutableStateFlow(InstitutionSettings())
    override val institutionSettings: StateFlow<InstitutionSettings> = _institutionSettings.asStateFlow()

    private var ticketSequence = 11
    private var serviceSequence = 1
    private var staffSequence = 1
    private var notificationSequence = 1
    private var appointmentSequence = 5

    init { refreshPositions() }

    fun findTicket(ticketId: String?): QueueTicket? = _queueTickets.value.find { it.id == ticketId }

    private fun currentService(service: RegistrarService): RegistrarService =
        _services.value.find { it.id == service.id }
            ?: throw IllegalArgumentException("This service no longer exists.")

    private fun refreshPositions() {
        var ahead = _queueTickets.value.count { it.status == QueueStatus.CALLED || it.status == QueueStatus.SERVING }
        _queueTickets.update { tickets ->
            tickets.map { ticket ->
                if (ticket.status == QueueStatus.WAITING || ticket.status == QueueStatus.SKIPPED) {
                    val count = ahead++
                    ticket.copy(positionInLine = count + 1, peopleAhead = count, estimatedWaitMinutes = count * 5)
                } else ticket.copy(peopleAhead = 0, positionInLine = if (ticket.status.isActive) 1 else 0, estimatedWaitMinutes = 0)
            }
        }
    }

    fun joinQueueForStudent(service: RegistrarService): QueueTicket {
        val current = currentService(service)
        require(current.isAvailable && current.isQueueEnabled) { "This service is not accepting queue tickets." }
        require(_queueTickets.value.none { it.studentId == _studentProfile.value.studentId && it.status.isActive }) {
            "You already have an active queue ticket."
        }
        require(_queueTickets.value.count { it.status.isActive } < _institutionSettings.value.maxDailyQueueLimit) {
            "The demo queue limit has been reached."
        }
        val student = _studentProfile.value
        val number = "R-%03d".format(Locale.US, ticketSequence++)
        val ticket = QueueTicket(
            id = "tkt-${number.lowercase(Locale.US)}", ticketNumber = number,
            serviceId = current.id, serviceName = current.name,
            studentName = student.fullName, studentId = student.studentId,
            status = QueueStatus.WAITING, positionInLine = 0, peopleAhead = 0,
            estimatedWaitMinutes = 0, timeJoined = currentFormattedTime(), waitingDurationText = "Just joined"
        )
        _queueTickets.update { it + ticket }
        _studentTicketId.value = ticket.id
        refreshPositions()
        addNotification("Joined Queue ($number)", "Demo ticket $number created for ${current.name}.")
        return requireNotNull(findTicket(ticket.id))
    }

    fun cancelStudentActiveTicket(ticketId: String) {
        val ticket = findTicket(ticketId) ?: throw IllegalArgumentException("Ticket not found.")
        require(ticket.studentId == _studentProfile.value.studentId) { "This is not your ticket." }
        updateTicketStatus(ticketId, QueueStatus.CANCELLED)
    }

    fun restoreDemoTicketR001() {
        val ticket = findTicket("tkt-r001") ?: throw IllegalArgumentException("Demo ticket not found.")
        require(_queueTickets.value.none { it.id != ticket.id && it.studentId == _studentProfile.value.studentId && it.status.isActive }) {
            "Finish or cancel your current ticket before restoring the sample."
        }
        _queueTickets.update { list -> list.map {
            if (it.id == ticket.id) it.copy(status = QueueStatus.WAITING, startTime = null, completedTime = null) else it
        } }
        _studentTicketId.value = ticket.id
        refreshPositions()
    }

    private fun validateBooking(service: RegistrarService, dateText: String, timeSlot: String, excludedId: String? = null): RegistrarService {
        val current = currentService(service)
        require(current.isAvailable && current.isAppointmentEnabled) { "This service is not accepting appointments." }
        DemoBookingRules.validateSchedule(dateText, timeSlot, LocalDate.now(clock), LocalTime.now(clock))
        require(_appointments.value.none {
            it.id != excludedId && it.status !in setOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW) &&
                it.dateText == dateText && it.timeSlot == timeSlot &&
                (it.serviceId == current.id || it.studentId == _studentProfile.value.studentId)
        }) { "This time conflicts with an existing appointment. Choose another slot." }
        return current
    }

    fun bookStudentAppointment(service: RegistrarService, dateText: String, timeSlot: String): Appointment {
        val current = validateBooking(service, dateText, timeSlot)
        val student = _studentProfile.value
        val ref = "A-%03d".format(Locale.US, appointmentSequence++)
        val appointment = Appointment(
            id = "apt-${ref.lowercase(Locale.US)}", referenceNumber = ref,
            serviceId = current.id, serviceName = current.name,
            studentName = student.fullName, studentId = student.studentId,
            dateText = dateText, timeSlot = timeSlot, status = AppointmentStatus.BOOKED,
            isToday = dateText == currentFormattedDate()
        )
        _appointments.update { it + appointment }
        syncAppointmentHistory(appointment)
        addNotification("Demo Appointment Booked ($ref)", "${current.name} scheduled for $dateText at $timeSlot.")
        return appointment
    }

    fun rescheduleStudentAppointment(id: String, service: RegistrarService, dateText: String, timeSlot: String): Appointment {
        val old = _appointments.value.find { it.id == id } ?: throw IllegalArgumentException("Appointment not found.")
        require(old.studentId == _studentProfile.value.studentId && old.status.canEdit) { "This appointment cannot be rescheduled." }
        val current = validateBooking(service, dateText, timeSlot, id)
        val updated = old.copy(serviceId = current.id, serviceName = current.name, dateText = dateText,
            timeSlot = timeSlot, status = AppointmentStatus.BOOKED, isToday = dateText == currentFormattedDate())
        _appointments.update { list -> list.map { if (it.id == id) updated else it } }
        syncAppointmentHistory(updated)
        addNotification("Demo Appointment Rescheduled", "${updated.referenceNumber}: $dateText at $timeSlot.")
        return updated
    }

    fun cancelStudentAppointment(id: String) {
        val apt = _appointments.value.find { it.id == id } ?: throw IllegalArgumentException("Appointment not found.")
        require(apt.studentId == _studentProfile.value.studentId && apt.status.canEdit) { "This appointment cannot be cancelled." }
        updateAppointmentStatus(id, AppointmentStatus.CANCELLED)
    }

    fun updateTicketStatus(ticketId: String, newStatus: QueueStatus) {
        val ticket = findTicket(ticketId) ?: throw IllegalArgumentException("Ticket not found.")
        require(ticket.status.canTransitionTo(newStatus)) { "Cannot change ${ticket.status.displayName} to ${newStatus.displayName}." }
        val updated = ticket.copy(status = newStatus,
            startTime = if (newStatus == QueueStatus.SERVING) currentFormattedTime() else ticket.startTime,
            completedTime = if (newStatus == QueueStatus.COMPLETED) currentFormattedTime() else ticket.completedTime)
        _queueTickets.update { list -> list.map { if (it.id == ticketId) updated else it } }
        refreshPositions()
        if (!newStatus.isActive) {
            val record = TransactionRecord(
                id = "trx-${ticket.id}", referenceNumber = ticket.ticketNumber, type = TransactionType.QUEUE,
                serviceName = ticket.serviceName, studentName = ticket.studentName,
                dateTimeText = "${currentFormattedDate()} • ${currentFormattedTime()}", queueStatus = newStatus,
                ownerId = if (ticket.studentId == _studentProfile.value.studentId) _studentProfile.value.id else ticket.studentId,
                serviceId = ticket.serviceId
            )
            _transactions.update { listOf(record) + it.filterNot { rec -> rec.id == record.id } }
        }
        if (ticket.studentId == _studentProfile.value.studentId) {
            addNotification("Ticket ${ticket.ticketNumber} — ${newStatus.displayName}", "Demo status updated to ${newStatus.displayName}.")
        }
    }

    fun callNextWaitingTicket(): QueueTicket? {
        val next = _queueTickets.value.firstOrNull { it.status == QueueStatus.WAITING } ?: return null
        updateTicketStatus(next.id, QueueStatus.CALLED)
        return findTicket(next.id)
    }

    private fun syncAppointmentHistory(apt: Appointment) {
        val record = TransactionRecord(
            id = "trx-${apt.id}", referenceNumber = apt.referenceNumber, type = TransactionType.APPOINTMENT,
            serviceName = apt.serviceName, studentName = apt.studentName, dateTimeText = "${apt.dateText} • ${apt.timeSlot}",
            appointmentStatus = apt.status,
            ownerId = if (apt.studentId == _studentProfile.value.studentId) _studentProfile.value.id else apt.studentId,
            serviceId = apt.serviceId
        )
        _transactions.update { listOf(record) + it.filterNot { rec -> rec.id == record.id } }
    }

    fun updateAppointmentStatus(appointmentId: String, newStatus: AppointmentStatus) {
        val apt = _appointments.value.find { it.id == appointmentId } ?: throw IllegalArgumentException("Appointment not found.")
        require(apt.status.canTransitionTo(newStatus)) { "Cannot change ${apt.status.displayName} to ${newStatus.displayName}." }
        val updated = apt.copy(status = newStatus)
        _appointments.update { list -> list.map { if (it.id == apt.id) updated else it } }
        syncAppointmentHistory(updated)
    }

    fun saveService(id: String?, name: String, description: String, requirementsText: String,
        iconType: ServiceIconType, isAvailable: Boolean, isQueueEnabled: Boolean, isAppointmentEnabled: Boolean, estimatedMinutes: Int
    ) {
        require(name.isNotBlank()) { "Service name is required." }
        require(estimatedMinutes in 1..480) { "Duration must be between 1 and 480 minutes." }
        require(id == null || _services.value.any { it.id == id }) { "Service not found." }
        val service = RegistrarService(id ?: "srv-demo-${serviceSequence++}", name.trim(), description.trim(),
            requirementsText.split(",", "\n").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf("Valid Student ID") },
            iconType, isAvailable, isQueueEnabled, isAppointmentEnabled, estimatedMinutes)
        _services.update { list -> if (id == null) list + service else list.map { if (it.id == id) service else it } }
    }

    fun toggleServiceAvailability(serviceId: String) {
        require(_services.value.any { it.id == serviceId }) { "Service not found." }
        _services.update { list -> list.map { if (it.id == serviceId) it.copy(isAvailable = !it.isAvailable) else it } }
    }

    private fun validateEmail(email: String) {
        require(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) { "Enter a valid email address." }
    }

    fun saveStaffMember(id: String?, fullName: String, email: String, counterName: String, isActive: Boolean) {
        require(fullName.trim().length >= 3) { "Enter a full staff name." }
        validateEmail(email)
        require(counterName.isNotBlank()) { "Counter / window is required." }
        require(id == null || _staffMembers.value.any { it.id == id }) { "Staff member not found." }
        require(_staffMembers.value.none { it.id != id && it.email.equals(email.trim(), true) }) { "A staff member already uses this email." }
        val staff = StaffProfile(id ?: "stf-demo-${staffSequence++}", fullName.trim(), email.trim(), counterName = counterName.trim(), isActive = isActive)
        _staffMembers.update { list -> if (id == null) list + staff else list.map { if (it.id == id) staff else it } }
        if (_currentStaffProfile.value.id == id) _currentStaffProfile.value = staff
    }

    fun toggleStaffActive(staffId: String) {
        require(_staffMembers.value.any { it.id == staffId }) { "Staff member not found." }
        _staffMembers.update { list -> list.map { if (it.id == staffId) it.copy(isActive = !it.isActive) else it } }
        _staffMembers.value.find { it.id == _currentStaffProfile.value.id }?.let { _currentStaffProfile.value = it }
    }

    fun updateStudentProfile(fullName: String, studentId: String, email: String) {
        require(fullName.trim().length >= 3) { "Enter your full name." }
        require(studentId.trim().length >= 5) { "Enter a valid Student ID." }
        validateEmail(email)
        val previous = _studentProfile.value
        _studentProfile.value = previous.copy(fullName = fullName.trim(), studentId = studentId.trim(), email = email.trim())
        _queueTickets.update { list -> list.map { if (it.studentId == previous.studentId) it.copy(studentName = fullName.trim(), studentId = studentId.trim()) else it } }
        _appointments.update { list -> list.map { if (it.studentId == previous.studentId) it.copy(studentName = fullName.trim(), studentId = studentId.trim()) else it } }
        _transactions.update { list -> list.map { if (it.ownerId == previous.id) it.copy(studentName = fullName.trim()) else it } }
    }

    fun updateInstitutionSettings(settings: InstitutionSettings) {
        require(settings.institutionName.isNotBlank() && settings.operatingDays.isNotBlank() && settings.operatingHours.isNotBlank()) { "Institution and schedule fields are required." }
        require(settings.maxDailyQueueLimit > 0) { "Queue limit must be positive." }
        _institutionSettings.value = settings
    }

    fun markAllNotificationsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    private fun addNotification(title: String, message: String) {
        val item = NotificationItem("notif-demo-${notificationSequence++}", title, message, "Just now")
        _notifications.update { listOf(item) + it }
    }

    fun getAnalyticsByService(periodIndex: Int): List<ServiceAnalyticsBar> {
        val completed = _transactions.value.filter { it.queueStatus == QueueStatus.COMPLETED || it.appointmentStatus == AppointmentStatus.COMPLETED }
        val today = LocalDate.now(clock)
        val from = when (periodIndex) {
            0 -> today.minusDays(today.dayOfWeek.value.toLong() - 1)
            1 -> today.withDayOfMonth(1)
            else -> today.minusMonths(6)
        }
        val counts = completed.filter {
            runCatching { !DemoBookingRules.parseDate(it.dateTimeText.substringBefore(" •")).isBefore(from) }.getOrDefault(false)
        }.groupingBy { record -> record.serviceId ?: _services.value.find { it.name == record.serviceName }?.id }.eachCount()
        return _services.value.map { ServiceAnalyticsBar(it.name, it.name.substringBefore(" "), counts[it.id] ?: 0) }
    }

    // --- Initial Fixtures Matching Reference Specification ---

    private fun initialServices(): List<RegistrarService> = listOf(
        RegistrarService(
            id = "srv-doc",
            name = "Document Request",
            description = "Request official certifications, diplomas, and academic forms",
            requirements = listOf("Valid Student ID", "Cleared Student Account", "Request Form"),
            iconType = ServiceIconType.DOCUMENT_REQUEST,
            isAvailable = true,
            isQueueEnabled = true,
            isAppointmentEnabled = true,
            estimatedMinutes = 10
        ),
        RegistrarService(
            id = "srv-enroll",
            name = "Enrollment Verification",
            description = "Verify current semester enrollment status and study load",
            requirements = listOf("Valid Student ID", "Current Registration Form"),
            iconType = ServiceIconType.ENROLLMENT_VERIFICATION,
            isAvailable = true,
            isQueueEnabled = true,
            isAppointmentEnabled = true,
            estimatedMinutes = 8
        ),
        RegistrarService(
            id = "srv-gmc",
            name = "Good Moral Certificate",
            description = "Apply for Certificate of Good Moral Character",
            requirements = listOf("Valid Student ID", "Official Receipt (if applicable)"),
            iconType = ServiceIconType.GOOD_MORAL,
            isAvailable = true,
            isQueueEnabled = true,
            isAppointmentEnabled = true,
            estimatedMinutes = 10
        ),
        RegistrarService(
            id = "srv-tor",
            name = "Transcript of Records",
            description = "Request official Transcript of Records (TOR) for evaluation or employment",
            requirements = listOf("Valid Student ID", "Exit Clearance", "2x2 ID Picture"),
            iconType = ServiceIconType.TRANSCRIPT,
            isAvailable = true,
            isQueueEnabled = true,
            isAppointmentEnabled = true,
            estimatedMinutes = 15
        ),
        RegistrarService(
            id = "srv-other",
            name = "Other Services",
            description = "General Registrar inquiries,authentication, and record updates",
            requirements = listOf("Valid Student ID", "Supporting Academic Documents"),
            iconType = ServiceIconType.OTHER,
            isAvailable = true,
            isQueueEnabled = true,
            isAppointmentEnabled = true,
            estimatedMinutes = 12
        )
    )

    private fun initialStaffMembers(): List<StaffProfile> = listOf(
        StaffProfile(
            id = "stf-001",
            fullName = "Maria Santos",
            email = "maria.santos@campusq.edu.ph",
            roleTitle = "Registrar Staff",
            counterName = "Window 1",
            isActive = true
        ),
        StaffProfile(
            id = "stf-002",
            fullName = "Pedro Reyes",
            email = "pedro.reyes@campusq.edu.ph",
            roleTitle = "Registrar Staff",
            counterName = "Window 2",
            isActive = true
        ),
        StaffProfile(
            id = "stf-003",
            fullName = "Ana Cruz",
            email = "ana.cruz@campusq.edu.ph",
            roleTitle = "Registrar Staff",
            counterName = "Window 3",
            isActive = true
        ),
        StaffProfile(
            id = "stf-004",
            fullName = "Luis Garcia",
            email = "luis.garcia@campusq.edu.ph",
            roleTitle = "Registrar Staff",
            counterName = "Window 4",
            isActive = false
        )
    )

    private fun initialQueueTickets(): List<QueueTicket> = listOf(
        QueueTicket(
            id = "tkt-r001",
            ticketNumber = "R-001",
            serviceId = "srv-doc",
            serviceName = "Document Request",
            studentName = "Juan Dela Cruz",
            studentId = "2023-10482",
            status = QueueStatus.WAITING,
            positionInLine = 4,
            peopleAhead = 3,
            estimatedWaitMinutes = 15,
            timeJoined = "9:15 AM",
            waitingDurationText = "12 min"
        ),
        QueueTicket(
            id = "tkt-r002",
            ticketNumber = "R-002",
            serviceId = "srv-enroll",
            serviceName = "Enrollment Verification",
            studentName = "Maria Santos",
            studentId = "2022-10921",
            status = QueueStatus.WAITING,
            positionInLine = 5,
            peopleAhead = 4,
            estimatedWaitMinutes = 20,
            timeJoined = "9:18 AM",
            waitingDurationText = "9 min"
        ),
        QueueTicket(
            id = "tkt-r003",
            ticketNumber = "R-003",
            serviceId = "srv-gmc",
            serviceName = "Good Moral Certificate",
            studentName = "Pedro Reyes",
            studentId = "2024-11204",
            status = QueueStatus.WAITING,
            positionInLine = 6,
            peopleAhead = 5,
            estimatedWaitMinutes = 25,
            timeJoined = "9:22 AM",
            waitingDurationText = "5 min"
        ),
        QueueTicket(
            id = "tkt-r004",
            ticketNumber = "R-004",
            serviceId = "srv-tor",
            serviceName = "Transcript of Records",
            studentName = "Ana Cruz",
            studentId = "2021-00843",
            status = QueueStatus.CALLED,
            positionInLine = 1,
            peopleAhead = 0,
            estimatedWaitMinutes = 0,
            timeJoined = "9:05 AM",
            waitingDurationText = "22 min"
        ),
        QueueTicket(
            id = "tkt-r005",
            ticketNumber = "R-005",
            serviceId = "srv-doc",
            serviceName = "Document Request",
            studentName = "Luis Garcia",
            studentId = "2022-04819",
            status = QueueStatus.SERVING,
            positionInLine = 1,
            peopleAhead = 0,
            estimatedWaitMinutes = 0,
            timeJoined = "8:55 AM",
            waitingDurationText = "18 min",
            startTime = "9:13 AM"
        ),
        QueueTicket(
            id = "tkt-r010",
            ticketNumber = "R-010",
            serviceId = "srv-enroll",
            serviceName = "Enrollment Verification",
            studentName = "Carla Mendoza",
            studentId = "2023-08411",
            status = QueueStatus.COMPLETED,
            positionInLine = 0,
            peopleAhead = 0,
            estimatedWaitMinutes = 0,
            timeJoined = "8:30 AM",
            waitingDurationText = "10 min",
            startTime = "8:40 AM",
            completedTime = "8:52 AM"
        )
    )

    private fun initialAppointments(): List<Appointment> {
        val today = currentFormattedDate()
        val tomorrow = LocalDate.now(clock).plusDays(1).format(dateFormatter)
        val nextDays = LocalDate.now(clock).plusDays(2).format(dateFormatter)

        return listOf(
            Appointment(
                id = "apt-a001",
                referenceNumber = "A-001",
                serviceId = "srv-doc",
                serviceName = "Document Request",
                studentName = "Juan Dela Cruz",
                studentId = "2023-10482",
                dateText = today,
                timeSlot = "10:00 AM",
                status = AppointmentStatus.CONFIRMED,
                isToday = true
            ),
            Appointment(
                id = "apt-a002",
                referenceNumber = "A-002",
                serviceId = "srv-enroll",
                serviceName = "Enrollment Verification",
                studentName = "Maria Santos",
                studentId = "2022-10921",
                dateText = today,
                timeSlot = "11:00 AM",
                status = AppointmentStatus.BOOKED,
                isToday = true
            ),
            Appointment(
                id = "apt-a003",
                referenceNumber = "A-003",
                serviceId = "srv-gmc",
                serviceName = "Good Moral Certificate",
                studentName = "Pedro Reyes",
                studentId = "2024-11204",
                dateText = tomorrow,
                timeSlot = "1:00 PM",
                status = AppointmentStatus.CONFIRMED,
                isToday = false
            ),
            Appointment(
                id = "apt-a004",
                referenceNumber = "A-004",
                serviceId = "srv-doc",
                serviceName = "Document Request",
                studentName = "Ana Cruz",
                studentId = "2021-00843",
                dateText = nextDays,
                timeSlot = "2:00 PM",
                status = AppointmentStatus.BOOKED,
                isToday = false
            )
        )
    }

    private fun initialTransactions(): List<TransactionRecord> {
        val today = currentFormattedDate()
        val yesterday = LocalDate.now(clock).minusDays(1).format(dateFormatter)
        val threeDaysAgo = LocalDate.now(clock).minusDays(3).format(dateFormatter)

        return listOf(
            TransactionRecord(
                id = "trx-1",
                referenceNumber = "R-010",
                type = TransactionType.QUEUE,
                serviceName = "Enrollment Verification",
                serviceId = "srv-enroll",
                studentName = "Carla Mendoza",
                ownerId = "2023-08411",
                dateTimeText = "$today • 8:52 AM",
                queueStatus = QueueStatus.COMPLETED
            ),
            TransactionRecord(
                id = "trx-2",
                referenceNumber = "A-101",
                type = TransactionType.APPOINTMENT,
                serviceName = "Document Request",
                serviceId = "srv-doc",
                studentName = "Juan Dela Cruz",
                dateTimeText = "$yesterday • 10:00 AM",
                appointmentStatus = AppointmentStatus.COMPLETED
            ),
            TransactionRecord(
                id = "trx-3",
                referenceNumber = "R-015",
                type = TransactionType.QUEUE,
                serviceName = "Good Moral Certificate",
                serviceId = "srv-gmc",
                studentName = "Juan Dela Cruz",
                dateTimeText = "$yesterday • 2:30 PM",
                queueStatus = QueueStatus.CANCELLED
            ),
            TransactionRecord(
                id = "trx-4",
                referenceNumber = "A-102",
                type = TransactionType.APPOINTMENT,
                serviceName = "Enrollment Verification",
                serviceId = "srv-enroll",
                studentName = "Juan Dela Cruz",
                dateTimeText = "$threeDaysAgo • 1:00 PM",
                appointmentStatus = AppointmentStatus.COMPLETED
            )
        )
    }

    private fun initialNotifications(): List<NotificationItem> = listOf(
        NotificationItem(
            id = "n-1",
            title = "Queue Ticket R-001 Active",
            message = "Your Document Request ticket is active in the demo queue.",
            timestamp = "10 min ago",
            isRead = false
        ),
        NotificationItem(
            id = "n-2",
            title = "Appointment A-001 Confirmed",
            message = "Your Document Request appointment at 10:00 AM is confirmed (Demo).",
            timestamp = "1 hr ago",
            isRead = false
        ),
        NotificationItem(
            id = "n-3",
            title = "Registrar Office Hours",
            message = "Office of the Registrar is open Monday to Friday, 8:00 AM – 5:00 PM.",
            timestamp = "Yesterday",
            isRead = true
        )
    )
}
