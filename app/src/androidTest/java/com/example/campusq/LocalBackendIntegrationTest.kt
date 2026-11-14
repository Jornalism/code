package com.example.campusq

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.campusq.data.local.LocalApi
import com.example.campusq.data.local.LocalApiException
import com.example.campusq.data.local.LocalCampusQRepository
import com.example.campusq.data.local.LocalSessionStore
import kotlinx.coroutines.async
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import com.example.campusq.data.local.jsonBody
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class LocalBackendIntegrationTest {
    private fun fixture(): JSONObject {
        assumeTrue("Explicit local-server fixture required.", InstrumentationRegistry.getArguments().getString("localBackend") == "true")
        return InstrumentationRegistry.getInstrumentation().context.assets.open("local-acceptance.json").bufferedReader().use { JSONObject(it.readText()) }
    }
    @Test fun independentAndroidSessionsSharePersistentQueueAppointmentsAndAuthorization(): Unit = runBlocking {
        val f=fixture()
        val origin=f.getString("baseUrl")
        val publicApi=LocalApi(origin){null}
        val id=UUID.randomUUID().toString()
        val secret=f.getString("password")
        val email="android-$id@example.test"
        val user=publicApi.request("POST","/api/auth/register",jsonBody("fullName" to "Android Integration Student","studentId" to "Android-$id","email" to email,"password" to secret))
        assertEquals("STUDENT",user.getJSONObject("user").getString("role"))
        val login=publicApi.request("POST","/api/auth/login",jsonBody("email" to email,"password" to secret))
        val studentToken=login.getString("token")
        val student=LocalApi(origin){studentToken}
        val staffLogin=publicApi.request("POST","/api/auth/login",jsonBody("email" to f.getString("staffEmail"),"password" to secret))
        val staff=LocalApi(origin){staffLogin.getString("token")}
        var changingCredential=studentToken
        val identityBoundApi=LocalApi(origin){changingCredential}
        val request=async(start=CoroutineStart.UNDISPATCHED){identityBoundApi.request("GET","/api/session")}
        changingCredential=staffLogin.getString("token")
        assertEquals(user.getJSONObject("user").getString("id"),request.await().getJSONObject("user").getString("id"))
        val adminLogin=publicApi.request("POST","/api/auth/login",jsonBody("email" to f.getString("adminEmail"),"password" to secret))
        val admin=LocalApi(origin){adminLogin.getString("token")}
        val serviceId=admin.request("POST","/api/services",jsonBody("name" to "Queue $id","description" to "Integration service","requirementsText" to "ID",
            "iconType" to "OTHER","isAvailable" to true,"isQueueEnabled" to true,"isAppointmentEnabled" to true,"estimatedMinutes" to 10)).getString("id")
        val key=UUID.randomUUID().toString()
        val ticket=student.request("POST","/api/tickets",jsonBody("serviceId" to serviceId),key)
        assertEquals(ticket.toString(),student.request("POST","/api/tickets",jsonBody("serviceId" to serviceId),key).toString())
        val ticketId=ticket.getString("id")
        fun findTicket(state:JSONObject):JSONObject {
            val rows=state.getJSONArray("queueTickets")
            return (0 until rows.length()).map { rows.getJSONObject(it) }.first { it.getString("id")==ticketId }
        }
        assertEquals("WAITING",findTicket(staff.request("GET","/api/state")).getString("status"))
        for(status in listOf("CALLED","SERVING","COMPLETED")){
            val current=findTicket(staff.request("GET","/api/state"))
            staff.request("PATCH","/api/tickets/$ticketId",jsonBody("status" to status,"version" to current.getInt("version")))
            assertEquals(status,findTicket(student.request("GET","/api/state")).getString("status"))
        }
        var date=LocalDate.now().plusDays(4)
        while(date.dayOfWeek in setOf(DayOfWeek.SATURDAY,DayOfWeek.SUNDAY))date=date.plusDays(1)
        // A private service for this test avoids fixtures reserving the same slot across reruns.
        val ownService=admin.request("POST","/api/services",jsonBody("name" to "Android $id","description" to "Integration service","requirementsText" to "ID",
            "iconType" to "OTHER","isAvailable" to true,"isQueueEnabled" to true,"isAppointmentEnabled" to true,"estimatedMinutes" to 10)).getString("id")
        val appointment=student.request("POST","/api/appointments",jsonBody("serviceId" to ownService,"dateText" to date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy",Locale.US)),"timeSlot" to "9:00 AM"),UUID.randomUUID().toString())
        val appointmentId=appointment.getString("id")
        staff.request("PATCH","/api/appointments/$appointmentId",jsonBody("status" to "CONFIRMED","version" to 1))
        val repository=LocalCampusQRepository(student)
        repository.refresh()
        assertTrue(repository.appointments.value.any { it.id==appointmentId && it.status.name=="CONFIRMED" })
        assertTrue(repository.transactions.value.isNotEmpty())
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val store=LocalSessionStore(context,origin)
        store.save(studentToken)
        assertTrue(studentToken == LocalSessionStore(context,origin).read())
        val encrypted=context.getSharedPreferences("campusq_local_session",0).all.values.joinToString()
        assertFalse(encrypted.contains(studentToken));assertFalse(encrypted.contains(secret))
        assertNull(LocalSessionStore(context,"http://other-host:8787").read())
        val denied=try{student.request("POST","/api/services",jsonBody());null}catch(e:LocalApiException){e}
        assertEquals(403,denied?.status)
        student.request("POST","/api/auth/logout",jsonBody())
        store.clear()
        val revoked=try{student.request("GET","/api/session");null}catch(e:LocalApiException){e}
        assertEquals(401,revoked?.status)
        staff.request("POST","/api/auth/logout",jsonBody());admin.request("POST","/api/auth/logout",jsonBody())
        Unit
    }
}
