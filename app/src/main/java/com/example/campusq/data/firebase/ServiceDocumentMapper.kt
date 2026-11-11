package com.example.campusq.data.firebase

import com.example.campusq.data.model.RegistrarService
import com.example.campusq.data.model.ServiceIconType

/** Strict wire-to-UI mapping; display strings never serve as database identifiers. */
object ServiceDocumentMapper {
    fun fromDocument(id: String, data: Map<String, Any?>): RegistrarService {
        require(id.isNotBlank() && id.length <= 128 && '/' !in id) { "Invalid service ID." }
        require(data["schemaVersion"] == 1L) { "Unsupported service schema." }
        fun text(key: String, maximum: Int): String {
            val value = data[key]
            require(value is String && value.isNotBlank() && value.length <= maximum) { "Invalid $key." }
            return value
        }
        fun flag(key: String): Boolean {
            val value = data[key]
            require(value is Boolean) { "Invalid $key." }
            return value
        }
        val minutes = data["estimatedMinutes"]
        require(minutes is Long && minutes in 1L..480L) { "Invalid estimatedMinutes." }
        val requirements = data["requirements"]
        require(requirements is List<*> && requirements.size <= 30 &&
            requirements.all { it is String && it.isNotBlank() && it.length <= 300 }) {
            "Invalid requirements."
        }
        return RegistrarService(
            id = id,
            name = text("name", 120),
            description = text("description", 2000),
            requirements = requirements.map { it as String },
            iconType = ServiceIconType.valueOf(text("iconType", 40)),
            isAvailable = flag("isAvailable"),
            isQueueEnabled = flag("isQueueEnabled"),
            isAppointmentEnabled = flag("isAppointmentEnabled"),
            estimatedMinutes = minutes.toInt()
        )
    }
}
