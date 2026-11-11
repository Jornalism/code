package com.example.campusq

import com.example.campusq.data.demo.DemoRepository
import com.example.campusq.data.demo.DemoServiceCatalogRepository
import com.example.campusq.data.firebase.ServiceDocumentMapper
import com.example.campusq.data.repository.CampusQStateSource
import com.example.campusq.data.repository.ServiceCatalogRepository
import com.example.campusq.viewmodel.CampusQViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FirebaseFoundationTest {
    private fun valid(): Map<String, Any?> = mapOf(
        "schemaVersion" to 1L, "name" to "Transcript", "description" to "Official transcript",
        "requirements" to listOf("Student ID"), "iconType" to "TRANSCRIPT",
        "isAvailable" to false, "isQueueEnabled" to true, "isAppointmentEnabled" to false,
        "estimatedMinutes" to 10L
    )

    @Test fun mapsStableIdentityAndPreservesDisabledFlags() {
        val result = ServiceDocumentMapper.fromDocument("stable-service-id", valid())
        assertEquals("stable-service-id", result.id)
        assertFalse(result.isAvailable)
        assertFalse(result.isAppointmentEnabled)
        assertEquals(10, result.estimatedMinutes)
    }

    @Test fun rejectsMissingAvailabilityRatherThanDefaultingToEnabled() {
        assertThrows(IllegalArgumentException::class.java) {
            ServiceDocumentMapper.fromDocument("service", valid() - "isAvailable")
        }
    }

    @Test fun rejectsUnsupportedSchemaAndIcon() {
        for (data in listOf(valid() + ("schemaVersion" to 2L), valid() + ("iconType" to "NEW"))) {
            assertThrows(IllegalArgumentException::class.java) { ServiceDocumentMapper.fromDocument("service", data) }
        }
    }

    @Test fun rejectsMalformedNumericTextArrayAndDocumentIdentifiers() {
        val malformed = listOf(
            valid() + ("estimatedMinutes" to 1.5), valid() + ("estimatedMinutes" to 0L),
            valid() + ("estimatedMinutes" to 481L), valid() + ("name" to ""),
            valid() + ("requirements" to listOf(1L)), valid() + ("requirements" to List(31) { "ID" })
        )
        for (data in malformed) assertThrows(IllegalArgumentException::class.java) {
            ServiceDocumentMapper.fromDocument("service", data)
        }
        assertThrows(IllegalArgumentException::class.java) { ServiceDocumentMapper.fromDocument("a/b", valid()) }
    }

    @Test fun asynchronousDemoCatalogUsesSharedStateInsteadOfCopyingFixtures() = runBlocking {
        val demo = DemoRepository()
        val catalog: ServiceCatalogRepository = DemoServiceCatalogRepository(demo)
        val first = catalog.fetchServices().first()
        demo.toggleServiceAvailability(first.id)
        assertEquals(!first.isAvailable, catalog.fetchServices().first { it.id == first.id }.isAvailable)
    }

    @Test fun viewModelStillObservesTheOriginalDemoStateContract() {
        val demo = DemoRepository()
        val source: CampusQStateSource = demo
        val viewModel = CampusQViewModel(demo)
        assertSame(source.services, viewModel.services)
        assertSame(source.queueTickets, viewModel.queueTickets)
        demo.updateStudentProfile("Campus Student", "2023-10482", "student@campusq.edu.ph")
        assertEquals(source.studentProfile.value, viewModel.studentProfile.value)
    }
}
