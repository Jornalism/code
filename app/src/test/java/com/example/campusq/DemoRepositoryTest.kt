package com.example.campusq

import com.example.campusq.data.demo.DemoRepository
import com.example.campusq.data.model.AppointmentStatus
import com.example.campusq.data.model.InstitutionSettings
import com.example.campusq.data.model.QueueStatus
import com.example.campusq.data.model.canTransitionTo
import com.example.campusq.data.model.isActive
import com.example.campusq.ui.navigation.CampusQRoutes
import com.example.campusq.viewmodel.CampusQViewModel
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class DemoRepositoryTest {
    private val clock = Clock.fixed(Instant.parse("2026-11-09T00:00:00Z"), ZoneId.of("Asia/Manila"))
    private fun repository() = DemoRepository(clock)
    private fun rejects(action: () -> Unit) {
        assertThrows(IllegalArgumentException::class.java, action)
    }
    private fun disable(repo: DemoRepository, queue: Boolean = true, appointment: Boolean = true) {
        val service = repo.services.value.first()
        repo.saveService(service.id, service.name, service.description, service.requirements.joinToString(","),
            service.iconType, true, queue, appointment, service.estimatedMinutes)
    }

    @Test fun allQueueTransitionsMatchThePolicy() {
        val allowed = setOf(
            QueueStatus.WAITING to QueueStatus.CALLED, QueueStatus.WAITING to QueueStatus.CANCELLED, QueueStatus.WAITING to QueueStatus.EXPIRED,
            QueueStatus.CALLED to QueueStatus.SERVING, QueueStatus.CALLED to QueueStatus.SKIPPED, QueueStatus.CALLED to QueueStatus.NO_SHOW, QueueStatus.CALLED to QueueStatus.CANCELLED,
            QueueStatus.SERVING to QueueStatus.COMPLETED, QueueStatus.SERVING to QueueStatus.NO_SHOW,
            QueueStatus.SKIPPED to QueueStatus.CALLED, QueueStatus.SKIPPED to QueueStatus.CANCELLED, QueueStatus.SKIPPED to QueueStatus.EXPIRED
        )
        QueueStatus.entries.forEach { from -> QueueStatus.entries.forEach { to ->
            assertEquals("$from -> $to", from to to in allowed, from.canTransitionTo(to))
        } }
    }

    @Test fun allAppointmentTransitionsMatchThePolicy() {
        val allowed = setOf(
            AppointmentStatus.BOOKED to AppointmentStatus.CONFIRMED, AppointmentStatus.BOOKED to AppointmentStatus.CHECKED_IN, AppointmentStatus.BOOKED to AppointmentStatus.CANCELLED,
            AppointmentStatus.CONFIRMED to AppointmentStatus.CHECKED_IN, AppointmentStatus.CONFIRMED to AppointmentStatus.CANCELLED, AppointmentStatus.CONFIRMED to AppointmentStatus.NO_SHOW,
            AppointmentStatus.CHECKED_IN to AppointmentStatus.SERVING, AppointmentStatus.CHECKED_IN to AppointmentStatus.NO_SHOW,
            AppointmentStatus.SERVING to AppointmentStatus.COMPLETED
        )
        AppointmentStatus.entries.forEach { from -> AppointmentStatus.entries.forEach { to ->
            assertEquals("$from -> $to", from to to in allowed, from.canTransitionTo(to))
        } }
    }

    @Test fun unknownTicketDoesNotResolveToAnotherStudent() {
        val repo = repository()
        val before = repo.queueTickets.value
        assertNull(repo.findTicket("missing"))
        assertNull(repo.findTicket(null))
        rejects { repo.updateTicketStatus("missing", QueueStatus.CALLED) }
        assertEquals(before, repo.queueTickets.value)
    }

    @Test fun cannotCompleteWaitingTicketOrReopenTerminalTicket() {
        val repo = repository()
        rejects { repo.updateTicketStatus("tkt-r001", QueueStatus.COMPLETED) }
        repo.updateTicketStatus("tkt-r001", QueueStatus.CANCELLED)
        rejects { repo.updateTicketStatus("tkt-r001", QueueStatus.CALLED) }
        assertEquals(QueueStatus.CANCELLED, repo.findTicket("tkt-r001")?.status)
    }

    @Test fun queueLifecycleUpdatesSharedTicketAndHistory() {
        val repo = repository()
        repo.updateTicketStatus("tkt-r001", QueueStatus.CALLED)
        repo.updateTicketStatus("tkt-r001", QueueStatus.SERVING)
        assertEquals("8:00 AM", repo.findTicket("tkt-r001")?.startTime)
        repo.updateTicketStatus("tkt-r001", QueueStatus.COMPLETED)
        val ticket = repo.findTicket("tkt-r001")!!
        assertEquals(QueueStatus.COMPLETED, ticket.status)
        assertEquals("8:00 AM", ticket.completedTime)
        assertEquals(QueueStatus.COMPLETED, repo.transactions.value.single { it.referenceNumber == "R-001" }.queueStatus)
    }

    @Test fun skipAndRecallKeepTicketActive() {
        val repo = repository()
        repo.updateTicketStatus("tkt-r001", QueueStatus.CALLED)
        repo.updateTicketStatus("tkt-r001", QueueStatus.SKIPPED)
        assertTrue(repo.findTicket("tkt-r001")!!.status.isActive)
        repo.updateTicketStatus("tkt-r001", QueueStatus.CALLED)
        assertEquals(QueueStatus.CALLED, repo.findTicket("tkt-r001")!!.status)
    }

    @Test fun duplicateActiveTicketIsRejectedWithoutChangingState() {
        val repo = repository()
        val before = repo.queueTickets.value
        rejects { repo.joinQueueForStudent(repo.services.value.first()) }
        assertEquals(before, repo.queueTickets.value)
    }

    @Test fun fifoAndPositionsStayConsistentAfterJoinAndCall() {
        val repo = repository()
        repo.cancelStudentActiveTicket("tkt-r001")
        val ticket = repo.joinQueueForStudent(repo.services.value.first())
        assertEquals("R-011", ticket.ticketNumber)
        assertEquals("R-002", repo.callNextWaitingTicket()?.ticketNumber)
        assertTrue(repo.findTicket(ticket.id)!!.peopleAhead > 0)
        assertEquals(ticket.id, repo.studentTicketId.value)
    }

    @Test fun repeatedJoinsUseUniqueNumbersIncludingSeededTickets() {
        val repo = repository()
        repeat(6) {
            repo.cancelStudentActiveTicket(requireNotNull(repo.studentTicketId.value))
            repo.joinQueueForStudent(repo.services.value.first())
        }
        assertEquals(repo.queueTickets.value.size, repo.queueTickets.value.map { it.ticketNumber }.toSet().size)
        assertTrue("A reused daily number must preserve older history",
            repo.transactions.value.any { it.id == "trx-3" && it.referenceNumber == "R-015" })
        assertEquals(2, repo.transactions.value.count { it.referenceNumber == "R-015" })
    }

    @Test fun disabledOrStaleServiceCannotJoinOrBook() {
        val repo = repository()
        val stale = repo.services.value.first()
        repo.cancelStudentActiveTicket("tkt-r001")
        repo.toggleServiceAvailability(stale.id)
        rejects { repo.joinQueueForStudent(stale) }
        rejects { repo.bookStudentAppointment(stale, "Nov 10, 2026", "9:00 AM") }
        rejects { repo.bookStudentAppointment(stale.copy(id = "missing"), "Nov 10, 2026", "9:00 AM") }
    }

    @Test fun queueAndAppointmentFlagsAreIndependentlyEnforced() {
        val repo = repository()
        repo.cancelStudentActiveTicket("tkt-r001")
        disable(repo, queue = false)
        rejects { repo.joinQueueForStudent(repo.services.value.first()) }
        repo.bookStudentAppointment(repo.services.value.first(), "Nov 10, 2026", "9:00 AM")
        disable(repo, appointment = false)
        rejects { repo.bookStudentAppointment(repo.services.value.first(), "Nov 10, 2026", "11:00 AM") }
    }

    @Test fun invalidPastWeekendAndUnavailableAppointmentTimesAreRejected() {
        val repo = repository()
        val service = repo.services.value.first()
        listOf("invalid", "Nov 08, 2026", "Nov 14, 2026", "Feb 30, 2027").forEach { date ->
            rejects { repo.bookStudentAppointment(service, date, "9:00 AM") }
        }
        rejects { repo.bookStudentAppointment(service, "Nov 10, 2026", "3:00 PM") }
        val evening = DemoRepository(Clock.fixed(Instant.parse("2026-11-09T10:00:00Z"), ZoneId.of("Asia/Manila")))
        rejects { evening.bookStudentAppointment(evening.services.value.first(), "Nov 09, 2026", "9:00 AM") }
    }

    @Test fun serviceAndStudentAppointmentConflictsAreRejected() {
        val repo = repository()
        val service = repo.services.value.first()
        val before = repo.appointments.value
        rejects { repo.bookStudentAppointment(service, "Nov 09, 2026", "10:00 AM") }
        rejects { repo.bookStudentAppointment(repo.services.value[1], "Nov 09, 2026", "10:00 AM") }
        rejects { repo.bookStudentAppointment(repo.services.value[1], "Nov 09, 2026", "11:00 AM") }
        assertEquals(before, repo.appointments.value)
    }

    @Test fun cancellationReleasesSlotAndUpdatesHistory() {
        val repo = repository()
        val service = repo.services.value.first()
        val booked = repo.bookStudentAppointment(service, "Nov 10, 2026", "9:00 AM")
        repo.cancelStudentAppointment(booked.id)
        assertEquals(AppointmentStatus.CANCELLED, repo.transactions.value.single { it.referenceNumber == booked.referenceNumber }.appointmentStatus)
        val newBooking = repo.bookStudentAppointment(service, "Nov 10, 2026", "9:00 AM")
        assertNotEquals(booked.id, newBooking.id)
    }

    @Test fun reschedulingKeepsReferenceAndReplacesHistory() {
        val repo = repository()
        val updated = repo.rescheduleStudentAppointment("apt-a001", repo.services.value.first(), "Nov 10, 2026", "11:00 AM")
        assertEquals("A-001", updated.referenceNumber)
        assertEquals(AppointmentStatus.BOOKED, updated.status)
        assertEquals("Nov 10, 2026 • 11:00 AM", repo.transactions.value.single { it.referenceNumber == "A-001" }.dateTimeText)
    }

    @Test fun rescheduleConflictLeavesOriginalIntact() {
        val repo = repository()
        val before = repo.appointments.value
        rejects { repo.rescheduleStudentAppointment("apt-a001", repo.services.value[1], "Nov 09, 2026", "11:00 AM") }
        assertEquals(before, repo.appointments.value)
    }

    @Test fun cannotEditOtherStudentOrCheckedInAppointment() {
        val repo = repository()
        rejects { repo.cancelStudentAppointment("apt-a002") }
        rejects { repo.rescheduleStudentAppointment("apt-a002", repo.services.value.first(), "Nov 10, 2026", "9:00 AM") }
        repo.updateAppointmentStatus("apt-a001", AppointmentStatus.CHECKED_IN)
        rejects { repo.cancelStudentAppointment("apt-a001") }
        rejects { repo.rescheduleStudentAppointment("apt-a001", repo.services.value.first(), "Nov 10, 2026", "9:00 AM") }
    }

    @Test fun appointmentLifecycleAndUnknownIdAreValidated() {
        val repo = repository()
        rejects { repo.updateAppointmentStatus("missing", AppointmentStatus.COMPLETED) }
        rejects { repo.updateAppointmentStatus("apt-a002", AppointmentStatus.COMPLETED) }
        repo.updateAppointmentStatus("apt-a001", AppointmentStatus.CHECKED_IN)
        repo.updateAppointmentStatus("apt-a001", AppointmentStatus.SERVING)
        repo.updateAppointmentStatus("apt-a001", AppointmentStatus.COMPLETED)
        rejects { repo.updateAppointmentStatus("apt-a001", AppointmentStatus.NO_SHOW) }
        assertEquals(AppointmentStatus.COMPLETED, repo.transactions.value.single { it.referenceNumber == "A-001" }.appointmentStatus)
    }

    @Test fun invalidEditableFormsLeaveDataUnchanged() {
        val repo = repository()
        val profile = repo.studentProfile.value
        rejects { repo.updateStudentProfile("", "1", "bad") }
        rejects { repo.saveStaffMember(null, "Valid Name", "invalid", "Window 1", true) }
        rejects { repo.updateInstitutionSettings(InstitutionSettings(institutionName = "")) }
        val service = repo.services.value.first()
        rejects { repo.saveService(service.id, service.name, "", "", service.iconType, true, true, true, 0) }
        assertEquals(profile, repo.studentProfile.value)
    }

    @Test fun profileAndCurrentStaffEditsUpdateSharedState() {
        val repo = repository()
        repo.updateStudentProfile("New Student Name", "2026-12345", "new@campusq.edu.ph")
        assertEquals("New Student Name", repo.findTicket("tkt-r001")?.studentName)
        assertEquals("2026-12345", repo.appointments.value.first { it.id == "apt-a001" }.studentId)
        repo.saveStaffMember("stf-001", "Maria Santos", "maria.santos@campusq.edu.ph", "Window 4", false)
        assertEquals(repo.staffMembers.value.first(), repo.currentStaffProfile.value)
    }

    @Test fun queueLimitAndTicketOwnershipAreEnforced() {
        val repo = repository()
        rejects { repo.cancelStudentActiveTicket("tkt-r002") }
        repo.cancelStudentActiveTicket("tkt-r001")
        repo.updateInstitutionSettings(InstitutionSettings(maxDailyQueueLimit = 1))
        rejects { repo.joinQueueForStudent(repo.services.value.first()) }
    }

    @Test fun viewModelReportsRejectedActionsAndSharesRepositoryState() {
        val repo = repository()
        val vm = CampusQViewModel(repo)
        assertNull(vm.joinQueue(repo.services.value.first()))
        assertTrue(vm.snackbarMessage.value!!.contains("active"))
        vm.cancelStudentTicket("tkt-r001")
        val ticket = vm.joinQueue(repo.services.value.first())!!
        assertEquals(ticket, repo.findTicket(ticket.id))
        vm.updateTicketStatus("missing", QueueStatus.CALLED)
        assertEquals("Ticket not found.", vm.snackbarMessage.value)
    }

    @Test fun roleTabsAreMappedToTheirOwnHomeAndDetailsAreNotTabs() {
        CampusQRoutes.roleTabs.forEach {
            assertNotNull(CampusQRoutes.roleHome(it))
            assertEquals(it.substringBefore('_'), CampusQRoutes.roleHome(it)!!.substringBefore('_'))
        }
        assertNull(CampusQRoutes.roleHome(CampusQRoutes.LOGIN))
        assertFalse(CampusQRoutes.STAFF_TICKET_DETAILS in CampusQRoutes.roleTabs)
        assertFalse(CampusQRoutes.STUDENT_SERVICES in CampusQRoutes.roleTabs)
    }

    @Test fun reusedAppointmentReferencePreservesEarlierHistory() {
        val repo = repository()
        var date = java.time.LocalDate.of(2027, 1, 4)
        repeat(97) {
            repo.bookStudentAppointment(repo.services.value.first(),
                date.format(com.example.campusq.data.model.DemoBookingRules.dateFormatter), "9:00 AM")
            do { date = date.plusDays(1) } while (date.dayOfWeek.value >= 6)
        }
        assertTrue(repo.transactions.value.any { it.id == "trx-2" && it.referenceNumber == "A-101" })
        assertEquals(2, repo.transactions.value.count { it.referenceNumber == "A-101" })
    }

    @Test fun serviceRenamePreservesAnalyticsCountsAndNewCompletions() {
        val repo = repository()
        val service = repo.services.value.first()
        val before = repo.getAnalyticsByService(1).first { it.serviceName == service.name }.count
        assertTrue(before > 0)
        repo.saveService(service.id, "Renamed Registrar Service", service.description, "",
            service.iconType, true, true, true, service.estimatedMinutes)
        assertEquals(before, repo.getAnalyticsByService(1).first { it.serviceName == "Renamed Registrar Service" }.count)
        repo.updateTicketStatus("tkt-r001", QueueStatus.CALLED)
        repo.updateTicketStatus("tkt-r001", QueueStatus.SERVING)
        repo.updateTicketStatus("tkt-r001", QueueStatus.COMPLETED)
        assertEquals(before + 1, repo.getAnalyticsByService(1).first { it.serviceName == "Renamed Registrar Service" }.count)
    }

    @Test fun localAnalyticsCountCompletedRecordsWithoutDemoOffsets() {
        val repo = repository()
        val before = repo.getAnalyticsByService(0).sumOf { it.count }
        repo.updateTicketStatus("tkt-r001", QueueStatus.CALLED)
        repo.updateTicketStatus("tkt-r001", QueueStatus.SERVING)
        repo.updateTicketStatus("tkt-r001", QueueStatus.COMPLETED)
        assertEquals(before + 1, repo.getAnalyticsByService(0).sumOf { it.count })
    }
}
