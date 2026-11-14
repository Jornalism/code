package com.example.campusq

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.campusq.data.firebase.FirebaseBackend
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/** Explicit read-only checks. Never signs in, writes data, calls functions or requests FCM tokens. */
@RunWith(AndroidJUnit4::class)
class FirebaseCloudVerificationTest {
    private fun configuredBackend(): FirebaseBackend {
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue("Requires genuine config and explicit cloud opt-in.",
            arguments.getString("firebaseCloudVerification") == "true")
        val expectedProjectId = requireNotNull(arguments.getString("firebaseCloudProjectId"))
        val expectedAppId = requireNotNull(arguments.getString("firebaseCloudAppId"))
        require(expectedProjectId.isNotBlank() && !expectedProjectId.startsWith("demo-"))
        require(expectedAppId.isNotBlank())
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.campusq", context.packageName)
        val backend = FirebaseBackend.fromConfiguration(context, "(default)", "asia-southeast1")
        assertEquals(FirebaseApp.DEFAULT_APP_NAME, backend.app.name)
        assertEquals(expectedProjectId, backend.app.options.projectId)
        assertEquals(expectedAppId, backend.app.options.applicationId)
        assertEquals("firestore.googleapis.com", backend.firestore.firestoreSettings.host)
        assertTrue(backend.firestore.firestoreSettings.isSslEnabled)
        assertNull("Refusing cloud probe with an existing signed-in user.", backend.auth.currentUser)
        return backend
    }

    @Test fun configuredSdkMatchesRegisteredAndroidApp() {
        configuredBackend()
    }

    @Test fun verifiedDatabaseDeniesUnauthenticatedServerRead() {
        val arguments = InstrumentationRegistry.getArguments()
        val backend = configuredBackend()
        // Must first confirm the real (default) database through the authenticated metadata API.
        assumeTrue("Database metadata/API must be independently verified before a cloud read.",
            arguments.getString("firebaseCloudDatabaseVerified") == "true")
        val failure = assertThrows(ExecutionException::class.java) {
            Tasks.await(backend.firestore.collection("services").limit(1).get(Source.SERVER),
                20, TimeUnit.SECONDS)
        }.cause
        assertTrue("Expected rules denial, got " + failure, failure is FirebaseFirestoreException)
        val message = failure?.message.orEmpty().lowercase()
        assertFalse("A disabled API is not a successful database connectivity check.",
            message.contains("api has not been used") || message.contains("service_disabled") ||
                message.contains("api is disabled"))
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED,
            (failure as FirebaseFirestoreException).code)
    }
}
