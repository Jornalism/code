package com.example.campusq.data.repository

import com.example.campusq.data.model.*
import kotlinx.coroutines.flow.StateFlow

/** UI state contract. Demo command execution stays local until asynchronous feature migration. */
interface CampusQStateSource {
    val studentProfile: StateFlow<StudentProfile>
    val currentStaffProfile: StateFlow<StaffProfile>
    val services: StateFlow<List<RegistrarService>>
    val staffMembers: StateFlow<List<StaffProfile>>
    val queueTickets: StateFlow<List<QueueTicket>>
    val studentTicketId: StateFlow<String?>
    val appointments: StateFlow<List<Appointment>>
    val transactions: StateFlow<List<TransactionRecord>>
    val notifications: StateFlow<List<NotificationItem>>
    val institutionSettings: StateFlow<InstitutionSettings>
}
