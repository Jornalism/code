package com.example.campusq

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.campusq.data.firebase.LocalFirebaseBackend
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FirebaseEmulatorFoundationTest {
    @Test fun explicitDebugConnectionReachesLocalAuthAndFirestoreAndRulesDenyAccess() = runBlocking {
        assumeTrue("Run inside Firebase emulators:exec with firebaseEmulators=true",
            InstrumentationRegistry.getArguments().getString("firebaseEmulators") == "true")
        val backend = LocalFirebaseBackend.create(InstrumentationRegistry.getInstrumentation().targetContext)
        try {
            assertEquals(LocalFirebaseBackend.PROJECT_ID, backend.app.options.projectId)
            val user = Tasks.await(backend.auth.signInAnonymously(), 30, TimeUnit.SECONDS).user
            assertNotNull(user)
            try {
                backend.serviceCatalog.fetchServices()
                fail("Closed rules must deny catalog reads.")
            } catch (error: FirebaseFirestoreException) {
                assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, error.code)
            }
            // Android callable context also requests a non-emulated IID/FCM token.
            // Verify that client path after genuine project setup; Node suite tests Functions locally.
        } finally {
            backend.auth.signOut()
            Tasks.await(backend.firestore.terminate(), 10, TimeUnit.SECONDS)
            backend.app.delete()
        }
    }
}
