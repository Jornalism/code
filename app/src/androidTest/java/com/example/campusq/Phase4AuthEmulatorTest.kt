package com.example.campusq

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.campusq.data.auth.AuthFailure
import com.example.campusq.data.auth.AuthProblem
import com.example.campusq.data.auth.ClosedUserProfileRepository
import com.example.campusq.data.auth.FirebaseAuthRepository
import com.example.campusq.data.auth.ProfileVerification
import com.example.campusq.data.auth.SessionPolicy
import com.example.campusq.data.auth.SessionState
import com.example.campusq.data.firebase.LocalFirebaseBackend
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class Phase4AuthEmulatorTest {
    private fun requireEmulator() {
        assumeTrue("Requires explicitly configured local Firebase emulators",
            InstrumentationRegistry.getArguments().getString("firebaseEmulators") == "true")
    }

    @Test fun sdkRegistrationVerificationRecoverySessionRestoreAndLogoutStayLocal() = runBlocking {
        requireEmulator()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val backend = LocalFirebaseBackend.create(context, "phase4-lifecycle-" + UUID.randomUUID())
        val email = "phase4-" + UUID.randomUUID() + "@example.test"
        val password = "Local-emulator-password-42"
        val replacement = "Local-replacement-password-43"
        try {
            assertEquals("demo-campusq-local", backend.app.options.projectId)
            backend.auth.signOut()
            var repository = FirebaseAuthRepository(backend.auth, emulatorOperations = true)
            repository.register("Local Student", email, password)
            val created = requireNotNull(repository.currentUser())
            assertFalse(created.emailVerified)
            assertNull(created.claims.role)
            repository.sendVerification()
            val verification = actionCode(email, "VERIFY_EMAIL")
            Tasks.await(backend.auth.applyActionCode(verification), 20, TimeUnit.SECONDS)
            repository.reload()
            val verified = requireNotNull(repository.currentUser(true))
            assertTrue(verified.emailVerified)
            assertTrue(SessionPolicy.evaluate(verified, ProfileVerification.Pending) is SessionState.ProvisioningPending)
            assertEquals(ProfileVerification.Unavailable, ClosedUserProfileRepository().verify(verified))

            // Reconstruct the repository over the SDK-owned session; no credentials are supplied.
            repository = FirebaseAuthRepository(backend.auth, emulatorOperations = true)
            assertEquals(created.uid, repository.currentUser()?.uid)

            repository.recoverPassword(email)
            val recovery = actionCode(email, "PASSWORD_RESET")
            Tasks.await(backend.auth.confirmPasswordReset(recovery, replacement), 20, TimeUnit.SECONDS)
            repository.signOut()
            assertNull(repository.currentUser())
            try {
                repository.login(email, password)
                fail("Old credentials unexpectedly accepted")
            } catch (failure: AuthFailure) {
                assertEquals(AuthProblem.INVALID_CREDENTIALS, failure.problem)
            }
            repository.login(email, replacement)
            assertEquals(created.uid, repository.currentUser()?.uid)
            repository.signOut()
            assertNull(repository.currentUser())
        } finally {
            backend.auth.signOut()
            Tasks.await(backend.firestore.terminate(), 10, TimeUnit.SECONDS)
            backend.app.delete()
        }
    }

    @Test fun defaultAdapterGateBlocksAccountAndEmailOperationsEvenWhenSdkIsConfigured() = runBlocking {
        requireEmulator()
        val backend = LocalFirebaseBackend.create(InstrumentationRegistry.getInstrumentation().targetContext,
            "phase4-gate-" + UUID.randomUUID())
        try {
            backend.auth.signOut()
            val repository = FirebaseAuthRepository(backend.auth)
            for (operation in listOf<suspend () -> Unit>(
                { repository.register("Local Student", "blocked@example.test", "local-password") },
                { repository.login("blocked@example.test", "local-password") },
                { repository.recoverPassword("blocked@example.test") },
                { repository.sendVerification() }
            )) {
                try { operation(); fail("Expected explicit approval gate") }
                catch (failure: AuthFailure) { assertEquals(AuthProblem.PROVIDER_UNAVAILABLE, failure.problem) }
            }
            assertNull(backend.auth.currentUser)
        } finally {
            Tasks.await(backend.firestore.terminate(), 10, TimeUnit.SECONDS)
            backend.app.delete()
        }
    }

    private fun actionCode(email: String, requestType: String): String {
        val connection = URL("http://10.0.2.2:9099/emulator/v1/projects/demo-campusq-local/oobCodes")
            .openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        return try {
            check(connection.responseCode == 200)
            val codes = JSONObject(connection.inputStream.bufferedReader().use { it.readText() }).getJSONArray("oobCodes")
            (codes.length() - 1 downTo 0).map { codes.getJSONObject(it) }
                .first { it.optString("email") == email && it.optString("requestType") == requestType }
                .getString("oobCode")
        } finally { connection.disconnect() }
    }
}
