package com.example.campusq.data.model

data class AppointmentSlot(val serviceId: String, val dateText: String, val timeSlot: String, val ownAppointmentId: String? = null)

fun List<AppointmentSlot>.isReserved(serviceId: String, dateText: String, timeSlot: String, editingId: String?): Boolean =
    any { it.serviceId == serviceId && it.dateText == dateText && it.timeSlot == timeSlot &&
        (editingId == null || it.ownAppointmentId != editingId) }
