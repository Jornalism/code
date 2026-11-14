package com.example.campusq

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.example.campusq.data.local.LocalApi
import com.example.campusq.data.local.LocalSessionStore
import com.example.campusq.data.local.jsonBody
import com.example.campusq.ui.navigation.CampusQLocalApp
import com.example.campusq.ui.theme.CampusQTheme
import com.example.campusq.viewmodel.LocalSessionViewModel
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class LocalBackendUiTest {
    @get:Rule val compose=createComposeRule()
    private fun fixture():JSONObject {
        assumeTrue(InstrumentationRegistry.getArguments().getString("localBackend")=="true")
        return InstrumentationRegistry.getInstrumentation().context.assets.open("local-acceptance.json").bufferedReader().use{JSONObject(it.readText())}
    }
    private fun waitText(text:String) {
        compose.waitUntil(40_000){compose.onAllNodesWithText(text,substring=true).fetchSemanticsNodes().isNotEmpty()}
    }
    private fun click(text:String,scroll:Boolean=false) {
        val node=compose.onNodeWithText(text)
        if(scroll)node.performScrollTo()
        node.performClick()
    }
    private fun selectService(name:String) {
        compose.onAllNodes(hasSetTextAction())[0].performTextInput(name)
        Espresso.closeSoftKeyboard();compose.waitForIdle()
        compose.onNode(hasText(name) and hasClickAction() and !hasSetTextAction()).performScrollTo().performClick()
    }
    private fun tab(label:String) {
        compose.onNode(hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role,Role.Tab)).performClick()
    }
    private fun session(f:JSONObject):LocalSessionViewModel {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val store=LocalSessionStore(context,f.getString("baseUrl"));store.clear()
        return LocalSessionViewModel(store,f.getString("baseUrl"))
    }
    private fun loginApi(f:JSONObject,email:String):LocalApi=runBlocking {
        val api=LocalApi(f.getString("baseUrl")){null}
        val response=api.request("POST","/api/auth/login",jsonBody("email" to email,"password" to f.getString("password")))
        LocalApi(f.getString("baseUrl")){response.getString("token")}
    }
    private fun createService(f:JSONObject,name:String):String=runBlocking {
        loginApi(f,f.getString("adminEmail")).request("POST","/api/services",jsonBody("name" to name,"description" to "UI acceptance service",
            "requirementsText" to "ID","iconType" to "OTHER","isAvailable" to true,"isQueueEnabled" to true,"isAppointmentEnabled" to true,"estimatedMinutes" to 10)).getString("id")
    }
    private fun showLogin(f:JSONObject,vm:LocalSessionViewModel=session(f)) {
        compose.setContent{CampusQTheme{CampusQLocalApp(session=vm)}}
        waitText("Welcome Back!")
        compose.onNodeWithText("Student").assertDoesNotExist()
        compose.onNodeWithText("Open Full 23-Screen UI Preview Hub →").assertDoesNotExist()
    }
    private fun loginUi(f:JSONObject,email:String) {
        compose.onAllNodes(hasSetTextAction())[0].performTextInput(email)
        compose.onAllNodes(hasSetTextAction())[1].performTextInput(f.getString("password"))
        click("Log In",true)
    }
    @Test fun studentRegistrationQueuePollingSessionRestoreAndLogoutUseServer() {
        val f=fixture();val id=UUID.randomUUID().toString();val email="ui-$id@example.test";val serviceName="UI Student $id"
        val serviceId=createService(f,serviceName)
        val vm=session(f);val host=mutableStateOf(vm)
        compose.setContent{CampusQTheme{CampusQLocalApp(session=host.value)}}
        waitText("Welcome Back!");click("Register")
        waitText("Create Account")
        val fields=compose.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("UI Student");fields[1].performTextInput("UI-$id");fields[2].performTextInput(email)
        fields[3].performTextInput(f.getString("password"));fields[4].performTextInput(f.getString("password"))
        click("Register",true)
        waitText("Registration Successful");waitText("Welcome Back!")
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val store=LocalSessionStore(context,f.getString("baseUrl"))
        compose.runOnIdle { assertNull(vm.ui.value.user);assertFalse(vm.hasUnconfirmedSavedSession) }
        assertNull(store.read())
        compose.onNodeWithText("Quick Actions").assertDoesNotExist()
        compose.onNodeWithText("Registrar Dashboard").assertDoesNotExist()
        compose.onNodeWithText("Admin Dashboard").assertDoesNotExist()
        runBlocking {
            try { vm.api.request("GET","/api/state");fail("Registration must not authorize protected data") }
            catch(error:com.example.campusq.data.local.LocalApiException) { assertEquals(401,error.status) }
        }
        // Reopening registration cannot restore either password from the completed form.
        click("Register");waitText("Create Account")
        for(index in listOf(3,4))assertEquals("",compose.onAllNodes(hasSetTextAction())[index]
            .fetchSemanticsNode().config[SemanticsProperties.EditableText].text)
        compose.onNodeWithContentDescription("Back to Login").performClick();waitText("Welcome Back!")
        loginUi(f,email);waitText("Quick Actions")
        assertNotNull(store.read())
        click("Join Queue")
        waitText("Registrar Services")
        selectService(serviceName)
        click("Join Queue")
        try { waitText("Queue Status");waitText("WAITING") }
        catch (error:Throwable) { throw AssertionError("Queue request did not appear: "+compose.onRoot().printToString(),error) }
        val student=loginApi(f,email);val staff=loginApi(f,f.getString("staffEmail"))
        val ticket=runBlocking{student.request("GET","/api/state").getJSONArray("queueTickets").getJSONObject(0)}
        val ticketId=ticket.getString("id")
        assertEquals(serviceId,ticket.getString("serviceId"))
        for((index,status) in listOf("CALLED","SERVING","COMPLETED").withIndex()){
            runBlocking{staff.request("PATCH","/api/tickets/$ticketId",jsonBody("status" to status,"version" to index+1))}
            waitText(if(status=="COMPLETED")"No Active Queue Ticket" else status)
        }
        tab("History");waitText("Transaction History");waitText(serviceName)
        compose.runOnIdle{host.value=LocalSessionViewModel(LocalSessionStore(context,f.getString("baseUrl")),f.getString("baseUrl"))}
        waitText("Quick Actions");tab("History");waitText(serviceName)
        tab("Profile");click("Logout",true);waitText("Sign out?")
        val buttons=compose.onAllNodesWithText("Sign out")
        buttons[buttons.fetchSemanticsNodes().lastIndex].performClick()
        waitText("Welcome Back!")
        assertNull(store.read())
        // Signing out the UI session must leave this independent session valid.
        assertEquals("STUDENT",runBlocking { student.request("GET","/api/session").getJSONObject("user").getString("role") })
        click("Register")
        Espresso.pressBack();waitText("Welcome Back!")
        compose.onNodeWithText("Quick Actions").assertDoesNotExist()
    }
    @Test fun duplicateRegistrationStaysOnRegistrationWithoutAuthorizingOrDeletingAccount() {
        val f=fixture();val id=UUID.randomUUID().toString();val email="duplicate-ui-$id@example.test"
        val publicApi=LocalApi(f.getString("baseUrl")){null}
        runBlocking { publicApi.request("POST","/api/auth/register",jsonBody("fullName" to "Duplicate UI Student",
            "studentId" to "DUP-$id","email" to email,"password" to f.getString("password"))) }
        val vm=session(f)
        showLogin(f,vm);click("Register");waitText("Create Account")
        val fields=compose.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("Duplicate UI Student");fields[1].performTextInput("DUP-$id");fields[2].performTextInput(email)
        fields[3].performTextInput(f.getString("password"));fields[4].performTextInput(f.getString("password"))
        click("Register",true);waitText("Account details are already registered.")
        compose.onNodeWithText("Create Account").assertExists()
        compose.onNodeWithText("Welcome Back!").assertDoesNotExist()
        compose.onNodeWithText("Quick Actions").assertDoesNotExist()
        compose.runOnIdle { assertNull(vm.ui.value.user);assertFalse(vm.ui.value.registrationSucceeded);assertFalse(vm.ui.value.busy) }
        assertNull(LocalSessionStore(InstrumentationRegistry.getInstrumentation().targetContext,f.getString("baseUrl")).read())
        for(index in listOf(3,4))assertEquals("",compose.onAllNodes(hasSetTextAction())[index]
            .fetchSemanticsNode().config[SemanticsProperties.EditableText].text)
        compose.onNodeWithContentDescription("Back to Login").performClick();waitText("Welcome Back!")
        loginUi(f,email);waitText("Quick Actions")
    }
    @Test fun staffProcessesActualStudentTicketThroughExistingScreens() {
        val f=fixture();val id=UUID.randomUUID().toString();val serviceId=createService(f,"UI Staff $id")
        val publicApi=LocalApi(f.getString("baseUrl")){null}
        val email="staff-client-$id@example.test"
        runBlocking{publicApi.request("POST","/api/auth/register",jsonBody("fullName" to "Waiting UI Student","studentId" to "WAIT-$id","email" to email,"password" to f.getString("password")))}
        val student=loginApi(f,email)
        val ticket=runBlocking{student.request("POST","/api/tickets",jsonBody("serviceId" to serviceId),UUID.randomUUID().toString())}
        showLogin(f);loginUi(f,f.getString("staffEmail"));waitText("Registrar Dashboard")
        click("View Queue",true);waitText("Queue Management")
        compose.onNodeWithText(ticket.getString("ticketNumber")).performScrollTo().performClick()
        waitText("Ticket Details")
        click("Call Ticket",true);click("Confirm")
        waitText("CALLED")
        click("Start Service",true)
        waitText("Service in Progress")
        click("Complete Service",true)
        val completeButtons=compose.onAllNodesWithText("Complete Service")
        completeButtons[completeButtons.fetchSemanticsNodes().lastIndex].performClick()
        compose.waitUntil(30_000){
            runBlocking{val rows=student.request("GET","/api/state").getJSONArray("queueTickets")
                (0 until rows.length()).map{rows.getJSONObject(it)}.any{it.getString("id")==ticket.getString("id") && it.getString("status")=="COMPLETED"}}
        }
        assertEquals(1,runBlocking{student.request("GET","/api/state").getJSONArray("transactions").length()})
    }
    @Test fun studentBooksReschedulesAndCancelsActualAppointmentThroughUi() {
        val f=fixture();val id=UUID.randomUUID().toString();val serviceName="UI Appointment $id";createService(f,serviceName)
        val email="booking-ui-$id@example.test"
        runBlocking{LocalApi(f.getString("baseUrl")){null}.request("POST","/api/auth/register",
            jsonBody("fullName" to "Booking UI Student","studentId" to "BOOK-$id","email" to email,"password" to f.getString("password")))}
        val student=loginApi(f,email);val staff=loginApi(f,f.getString("staffEmail"))
        showLogin(f);loginUi(f,email);waitText("Quick Actions");click("Join Queue")
        waitText("Registrar Services");selectService(serviceName)
        click("Book Appointment");waitText("Confirm Appointment")
        click("Confirm Appointment",true);click("Confirm Booking")
        waitText("Your Appointments")
        val row=runBlocking{student.request("GET","/api/state").getJSONArray("appointments").getJSONObject(0)}
        val appointmentId=row.getString("id")
        runBlocking{staff.request("PATCH","/api/appointments/$appointmentId",jsonBody("status" to "CONFIRMED","version" to 1))}
        waitText("CONFIRMED")
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Saved on the CampusQ server.").fetchSemanticsNodes().isEmpty() }
        click("Reschedule",true)
        val newSlot=compose.onNodeWithText("11:00 AM").assertIsEnabled().performScrollTo()
        compose.mainClock.advanceTimeBy(500);compose.waitForIdle()
        newSlot.performClick();newSlot.assertIsSelected()
        val rescheduleButton=compose.onNodeWithText("Confirm Rescheduling").assertIsEnabled().performScrollTo()
        compose.mainClock.advanceTimeBy(500);compose.waitForIdle()
        rescheduleButton.performClick()
        try { waitText("Confirm Booking") } catch (error: Throwable) {
            throw AssertionError("Missing reschedule confirmation. UI semantics: "+compose.onRoot().printToString(),error)
        }
        click("Confirm Booking")
        compose.waitUntil(30_000){runBlocking{student.request("GET","/api/state").getJSONArray("appointments").getJSONObject(0).getString("timeSlot")=="11:00 AM"}}
        waitText("Your Appointments")
        click("Cancel Appointment",true);waitText("Cancel Appointment?")
        val cancel=compose.onAllNodesWithText("Cancel Appointment")
        cancel[cancel.fetchSemanticsNodes().lastIndex].performClick()
        compose.waitUntil(30_000){runBlocking{student.request("GET","/api/state").getJSONArray("appointments").getJSONObject(0).getString("status")=="CANCELLED"}}
    }

    @Test fun staffReviewsActualAppointmentThroughUi() {
        val f=fixture();val id=UUID.randomUUID().toString();val serviceId=createService(f,"UI Review $id")
        val email="review-ui-$id@example.test"
        runBlocking{LocalApi(f.getString("baseUrl")){null}.request("POST","/api/auth/register",
            jsonBody("fullName" to "Review UI Student","studentId" to "REVIEW-$id","email" to email,"password" to f.getString("password")))}
        val student=loginApi(f,email)
        var day=java.time.LocalDate.now().plusDays(3)
        while(day.dayOfWeek in setOf(java.time.DayOfWeek.SATURDAY,java.time.DayOfWeek.SUNDAY))day=day.plusDays(1)
        val record=runBlocking{student.request("POST","/api/appointments",jsonBody("serviceId" to serviceId,
            "dateText" to day.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy",java.util.Locale.US)),
            "timeSlot" to "1:00 PM"),UUID.randomUUID().toString())}
        val appointmentId=record.getString("id");val reference="APT-"+appointmentId.take(8).uppercase()
        showLogin(f);loginUi(f,f.getString("staffEmail"));waitText("Registrar Dashboard");tab("Appointments")
        click("Upcoming")
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollToIndex)).performScrollToNode(hasText(reference,substring=true))
        compose.onNode(hasText(reference,substring=true) and hasClickAction()).performClick()
        waitText("Select a valid status transition:");click("Confirm")
        compose.waitUntil(30_000){runBlocking{val rows=student.request("GET","/api/state").getJSONArray("appointments")
            (0 until rows.length()).map{rows.getJSONObject(it)}.any{it.getString("id")==appointmentId && it.getString("status")=="CONFIRMED"}}}
    }

    @Test fun unavailableServerNeverProducesSuccessfulLogin() {
        val f=fixture();val context=InstrumentationRegistry.getInstrumentation().targetContext
        val origin="http://10.0.2.2:1";val store=LocalSessionStore(context,origin);store.clear()
        val vm=LocalSessionViewModel(store,origin)
        showLogin(f,vm)
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("unavailable@example.test")
        compose.onAllNodes(hasSetTextAction())[1].performTextInput(f.getString("password"))
        click("Log In",true);waitText("Cannot reach the CampusQ server")
        compose.onNodeWithText("Quick Actions").assertDoesNotExist()
        compose.onNodeWithText("Registrar Dashboard").assertDoesNotExist()
        compose.onNodeWithText("Admin Dashboard").assertDoesNotExist()
    }

    @Test fun administratorCreatesPersistentServiceAndStaffAccountThroughUi() {
        val f=fixture();val id=UUID.randomUUID().toString();val serviceName="UI Admin $id";val staffEmail="created-ui-$id@example.test"
        val queryAdmin=loginApi(f,f.getString("adminEmail"))
        showLogin(f);loginUi(f,f.getString("adminEmail"));waitText("Admin Dashboard")
        compose.onNodeWithContentDescription("Open Menu").performClick();click("Manage Services")
        waitText("Manage Services");click("Add Service")
        compose.onNodeWithText("Service Name").performTextInput(serviceName)
        compose.onNodeWithText("Description").performTextInput("Created from the administrator UI")
        compose.onNodeWithText("Requirements (comma-separated)").performTextInput("Student ID")
        click("Save Service",true)
        compose.waitUntil(30_000){
            runBlocking{val rows=queryAdmin.request("GET","/api/state").getJSONArray("services")
                (0 until rows.length()).any{rows.getJSONObject(it).getString("name")==serviceName}}
        }
        compose.onNodeWithContentDescription("Open Menu").performClick();click("Staff Management")
        waitText("Staff Management");click("Add Staff")
        compose.onNodeWithText("Full Name").performTextInput("Created UI Registrar")
        compose.onNodeWithText("Institutional Email").performTextInput(staffEmail)
        compose.onNodeWithText("Assigned Counter / Window").performTextReplacement("Window UI")
        compose.onNodeWithText("Initial password (at least 8 characters)").performTextInput(f.getString("password"))
        click("Save Staff Entry",true)
        compose.waitUntil(30_000){
            runBlocking{val rows=queryAdmin.request("GET","/api/state").getJSONArray("staffMembers")
                (0 until rows.length()).any{rows.getJSONObject(it).getString("email")==staffEmail}}
        }
        val account=runBlocking{LocalApi(f.getString("baseUrl")){null}.request("POST","/api/auth/login",jsonBody("email" to staffEmail,"password" to f.getString("password")))}
        assertEquals("STAFF",account.getJSONObject("user").getString("role"))
    }
}

