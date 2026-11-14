package com.example.campusq

import com.example.campusq.data.model.AppointmentSlot
import com.example.campusq.data.model.isReserved
import org.junit.Assert.*
import org.junit.Test

class AppointmentAvailabilityTest {
    private val occupied=listOf(AppointmentSlot("service","Nov 16, 2026","10:00 AM",null))
    @Test fun anonymousOtherStudentReservationBlocksNewBooking() {
        assertTrue(occupied.isReserved("service","Nov 16, 2026","10:00 AM",null))
    }
    @Test fun editingOwnReservationDoesNotBlockItselfButOthersStillBlock() {
        val own=listOf(AppointmentSlot("service","Nov 16, 2026","10:00 AM","mine"))
        assertFalse(own.isReserved("service","Nov 16, 2026","10:00 AM","mine"))
        assertTrue(occupied.isReserved("service","Nov 16, 2026","10:00 AM","mine"))
    }
    @Test fun differentServiceDateOrTimeRemainsAvailable() {
        assertFalse(occupied.isReserved("other","Nov 16, 2026","10:00 AM",null))
        assertFalse(occupied.isReserved("service","Nov 17, 2026","10:00 AM",null))
        assertFalse(occupied.isReserved("service","Nov 16, 2026","11:00 AM",null))
    }
}
