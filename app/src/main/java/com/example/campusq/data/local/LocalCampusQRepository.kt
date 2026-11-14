package com.example.campusq.data.local

import com.example.campusq.data.model.AppointmentSlot
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
import org.json.JSONArray
import org.json.JSONObject

class LocalCampusQRepository(val api: LocalApi) : CampusQStateSource {
    override val studentProfile = MutableStateFlow(StudentProfile("", "", "", "", "", ""))
    override val currentStaffProfile = MutableStateFlow(StaffProfile("", "", "", counterName = ""))
    override val services = MutableStateFlow<List<RegistrarService>>(emptyList())
    override val staffMembers = MutableStateFlow<List<StaffProfile>>(emptyList())
    override val queueTickets = MutableStateFlow<List<QueueTicket>>(emptyList())
    override val studentTicketId = MutableStateFlow<String?>(null)
    val nowServing = MutableStateFlow<Map<String, String>>(emptyMap())
    val reservedSlots = MutableStateFlow<List<AppointmentSlot>>(emptyList())
    override val appointments = MutableStateFlow<List<Appointment>>(emptyList())
    override val transactions = MutableStateFlow<List<TransactionRecord>>(emptyList())
    override val notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    override val institutionSettings = MutableStateFlow(InstitutionSettings(institutionName = "", pushAlertsEnabled = false))
    var analytics: List<List<ServiceAnalyticsBar>> = listOf(emptyList(), emptyList(), emptyList())
        private set
    var adminOverview: String = ""
        private set

    suspend fun refresh() {
        val json = api.request("GET", "/api/state")
        val serving = json.optJSONArray("nowServing") ?: JSONArray()
        nowServing.value = serving.objects { row -> row.getString("serviceId") to row.getString("ticketNumber") }
            .groupBy({ it.first }, { it.second }).mapValues { it.value.joinToString(", ") }
        reservedSlots.value = (json.optJSONArray("reservedSlots") ?: JSONArray()).objects { slot ->
            AppointmentSlot(slot.getString("serviceId"),slot.getString("dateText"),slot.getString("timeSlot"),slot.nullString("ownAppointmentId"))
        }
        val p = json.getJSONObject("studentProfile")
        studentProfile.value = StudentProfile(p.getString("id"), p.getString("fullName"), p.getString("studentId"), p.getString("email"), p.getString("program"), p.getString("yearLevel"))
        currentStaffProfile.value = staff(json.getJSONObject("currentStaffProfile"))
        services.value = json.getJSONArray("services").objects { s -> RegistrarService(s.getString("id"), s.getString("name"), s.getString("description"),
            s.getJSONArray("requirements").strings(), ServiceIconType.valueOf(s.getString("iconType")), s.getBoolean("isAvailable"),
            s.getBoolean("isQueueEnabled"), s.getBoolean("isAppointmentEnabled"), s.getInt("estimatedMinutes"), s.getInt("version")) }
        staffMembers.value = json.getJSONArray("staffMembers").objects(::staff)
        queueTickets.value = json.getJSONArray("queueTickets").objects { q -> QueueTicket(q.getString("id"), q.getString("ticketNumber"),
            q.getString("serviceId"), q.getString("serviceName"), q.getString("studentName"), q.getString("studentId"),
            QueueStatus.valueOf(q.getString("status")), q.getInt("positionInLine"), q.getInt("peopleAhead"), q.getInt("estimatedWaitMinutes"),
            q.getString("timeJoined"), q.getString("waitingDurationText"), q.nullString("startTime"), q.nullString("completedTime"), q.getInt("version")) }
        studentTicketId.value = json.nullString("studentTicketId")
        appointments.value = json.getJSONArray("appointments").objects { a -> Appointment(a.getString("id"), a.getString("referenceNumber"),
            a.getString("serviceId"), a.getString("serviceName"), a.getString("studentName"), a.getString("studentId"), a.getString("dateText"),
            a.getString("timeSlot"), AppointmentStatus.valueOf(a.getString("status")), a.getBoolean("isToday"), a.getInt("version")) }
        transactions.value = json.getJSONArray("transactions").objects { t -> TransactionRecord(t.getString("id"),t.getString("referenceNumber"),
            TransactionType.valueOf(t.getString("type")),t.getString("serviceName"),t.getString("studentName"),t.getString("dateTimeText"),
            t.nullString("queueStatus")?.let(QueueStatus::valueOf),t.nullString("appointmentStatus")?.let(AppointmentStatus::valueOf),
            t.getString("ownerId"),t.nullString("serviceId")) }
        notifications.value = json.getJSONArray("notifications").objects { n -> NotificationItem(n.getString("id"),n.getString("title"),
            n.getString("message"),n.getString("timestamp"),n.getBoolean("isRead")) }
        val s=json.getJSONObject("institutionSettings")
        institutionSettings.value=InstitutionSettings(s.getString("institutionName"),s.getString("operatingDays"),s.getString("operatingHours"),
            s.getInt("maxDailyQueueLimit"),s.getBoolean("pushAlertsEnabled"),s.getBoolean("smsFallbackEnabled"))
        analytics=(0..2).map { index -> json.getJSONArray("analytics").getJSONArray(index).objects { a ->
            ServiceAnalyticsBar(a.getString("serviceName"),a.getString("shortLabel"),a.getInt("count")) } }
        adminOverview = buildString {
            val users=json.getJSONArray("users")
            append("Accounts (").append(users.length()).append(")\n")
            users.objects { u -> append(u.getString("full_name")).append(" — ").append(u.getString("role")).append(" / ").append(u.getString("account_status")).append('\n') }
            append("\nCompleted transactions\n")
            transactions.value.forEach { record ->
                append(record.referenceNumber).append(" — ").append(record.serviceName).append(" — ").append(record.studentName)
                    .append(" — ").append(record.dateTimeText).append('\n')
            }
            append("\nRecent activity\n")
            json.getJSONArray("activity").objects { a -> append(a.getString("created_at")).append(" — ").append(a.getString("action")).append('\n') }
        }
    }
    private fun staff(s: JSONObject) = StaffProfile(s.getString("id"),s.getString("fullName"),s.getString("email"),s.getString("roleTitle"),s.getString("counterName"),s.getBoolean("isActive"))
}
private fun JSONObject.nullString(key: String): String? = if (isNull(key)) null else getString(key)
private fun JSONArray.strings(): List<String> = (0 until length()).map(::getString)
private fun <T> JSONArray.objects(block: (JSONObject) -> T): List<T> = (0 until length()).map { block(getJSONObject(it)) }
