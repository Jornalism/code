package com.example.campusq

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.campusq.data.local.LocalApi
import com.example.campusq.data.local.LocalSessionStore
import com.example.campusq.data.local.jsonBody
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class LocalMainActivityTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun actualLauncherRestoresEncryptedSessionAndTicketAfterClosingActivity() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("localBackend")=="true")
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val f=InstrumentationRegistry.getInstrumentation().context.assets.open("local-acceptance.json").bufferedReader().use{JSONObject(it.readText())}
        val origin=f.getString("baseUrl");val publicApi=LocalApi(origin){null};val id=UUID.randomUUID().toString()
        val email="launch-$id@example.test";val secret=f.getString("password")
        val token=runBlocking{
            publicApi.request("POST","/api/auth/register",jsonBody("fullName" to "Launcher Student","studentId" to "LAUNCH-$id","email" to email,"password" to secret))
            publicApi.request("POST","/api/auth/login",jsonBody("email" to email,"password" to secret)).getString("token")
        }
        val api=LocalApi(origin){token}
        val ticket=runBlocking{api.request("POST","/api/tickets",jsonBody("serviceId" to f.getString("serviceId")),UUID.randomUUID().toString())}
        val number=ticket.getString("ticketNumber")
        val store=LocalSessionStore(context,origin);store.clear();store.save(token)
        var scenario:ActivityScenario<MainActivity>?=null
        try {
            scenario=ActivityScenario.launch(MainActivity::class.java)
            compose.waitUntil(30_000){compose.onAllNodesWithText(number,substring=true).fetchSemanticsNodes().isNotEmpty()}
            scenario.close()
            scenario=ActivityScenario.launch(MainActivity::class.java)
            compose.waitUntil(30_000){compose.onAllNodesWithText(number,substring=true).fetchSemanticsNodes().isNotEmpty()}
        } finally {
            scenario?.close()
            runBlocking{api.request("POST","/api/auth/logout",jsonBody())}
            store.clear()
        }
    }
}
