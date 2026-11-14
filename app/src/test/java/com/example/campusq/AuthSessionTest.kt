package com.example.campusq

import com.example.campusq.data.auth.*
import com.example.campusq.data.model.UserRole
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class AuthSessionTest {
    private val cleanups = mutableListOf<() -> Unit>()
    private val unverified = AuthenticatedUser("auth-uid", "student@example.test", "Student Name", false)
    private fun verified(role: UserRole? = null, version: Long? = null) =
        unverified.copy(emailVerified = true, claims = AuthorizationClaims(role, version))

    private fun harness(
        user: AuthenticatedUser? = null,
        profile: ProfileVerification = ProfileVerification.Unavailable,
        profileThrows: Boolean = false
    ): Harness {
        val fake = FakeAuth(user)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        var clock = 1_000L
        val profiles = object : UserProfileRepository {
            override suspend fun verify(user: AuthenticatedUser): ProfileVerification {
                if (profileThrows) error("Do not expose internal profile errors")
                return profile
            }
        }
        val coordinator = SessionCoordinator(fake, profiles, scope) { clock }
        cleanups += { coordinator.close(); scope.cancel() }
        return Harness(fake, coordinator) { clock += it }
    }

    @After fun cleanup() { cleanups.forEach { it() } }
    private class Harness(val auth: FakeAuth, val session: SessionCoordinator, val advance: (Long) -> Unit)

    @Test fun signedOutRestorationIsExplicit() {
        assertEquals(SessionState.SignedOut, harness().session.ui.value.session)
    }

    @Test fun existingIdentityRestoresWithoutRequestingCredentials() {
        val h = harness(unverified)
        assertTrue(h.session.ui.value.session is SessionState.EmailVerificationRequired)
        assertEquals(0, h.auth.loginCalls)
        assertEquals(0, h.auth.registerCalls)
    }

    @Test fun unverifiedAdminClaimDoesNotAuthorize() {
        val h = harness(unverified.copy(claims = AuthorizationClaims(UserRole.ADMIN, 1)))
        assertTrue(h.session.ui.value.session is SessionState.EmailVerificationRequired)
        assertFalse(SessionPolicy.canOpenLiveRole(h.session.ui.value.session, UserRole.ADMIN))
    }

    @Test fun verifiedIdentityWithoutRoleRemainsPending() {
        assertTrue(harness(verified()).session.ui.value.session is SessionState.ProvisioningPending)
    }

    @Test fun invalidOrUnsupportedTokenClaimsRemainPending() {
        for (claims in listOf(
            mapOf("role" to "SUPER_ADMIN", "authorizationVersion" to 1L),
            mapOf("role" to "admin", "authorizationVersion" to 1L),
            mapOf("role" to "ADMIN", "authorizationVersion" to -1L),
            mapOf("role" to "ADMIN", "authorizationVersion" to 1.5),
            mapOf("role" to 1, "authorizationVersion" to 1L)
        )) {
            val user = verified().copy(claims = AuthorizationClaims.fromToken(claims))
            assertTrue(harness(user).session.ui.value.session is SessionState.ProvisioningPending)
        }
    }

    @Test fun missingAuthorizationVersionIsNotAccepted() {
        assertTrue(harness(verified(UserRole.STAFF)).session.ui.value.session is SessionState.ProvisioningPending)
    }

    @Test fun validClaimsStillNeedTrustedProfile() {
        assertTrue(harness(verified(UserRole.ADMIN, 1)).session.ui.value.session is SessionState.ProfileUnavailable)
    }

    @Test fun profileFailureDoesNotFallBackToDemoOrAuthorize() {
        assertTrue(harness(verified(UserRole.STAFF, 1), profileThrows = true).session.ui.value.session is SessionState.ProfileUnavailable)
    }

    @Test fun profileUidRoleVersionAndActiveStatusMustAgree() {
        val user = verified(UserRole.STUDENT, 2)
        val valid = TrustedProfile(user.uid, UserRole.STUDENT, 2, true)
        for (profile in listOf(valid.copy(uid = "other-uid"), valid.copy(role = UserRole.ADMIN),
            valid.copy(authorizationVersion = 1), valid.copy(active = false))) {
            assertTrue(harness(user, ProfileVerification.Verified(profile)).session.ui.value.session is SessionState.ProvisioningPending)
        }
    }

    @Test fun consistentTrustedProfileAuthorizesOnlyItsRole() {
        for (role in UserRole.entries) {
            val user = verified(role, 2)
            val h = harness(user, ProfileVerification.Verified(TrustedProfile(user.uid, role, 2, true)))
            assertTrue(SessionPolicy.canOpenLiveRole(h.session.ui.value.session, role))
            for (other in UserRole.entries.filter { it != role }) {
                assertFalse(SessionPolicy.canOpenLiveRole(h.session.ui.value.session, other))
            }
        }
    }

    @Test fun registrationRequestsVerificationButAssignsNoRoleOrProfile() {
        val h = harness()
        h.session.register(" Student Name ", "12345", " student@example.test ", "secret1", "secret1")
        assertEquals(1, h.auth.registerCalls)
        assertEquals(1, h.auth.verificationCalls)
        assertEquals("student@example.test", h.auth.lastEmail)
        assertNull(h.auth.user?.claims?.role)
        assertTrue(h.session.ui.value.session is SessionState.EmailVerificationRequired)
        assertEquals(AuthNotice.ACCOUNT_CREATED, h.session.ui.value.notice)
    }

    @Test fun invalidRegistrationNeverCallsRepository() {
        val h = harness()
        for (input in listOf(
            listOf("A", "12345", "student@example.test", "secret1"),
            listOf("Student", "1", "student@example.test", "secret1"),
            listOf("Student", "12345", "invalid", "secret1"),
            listOf("Student", "12345", "student@example.test", "short")
        )) h.session.register(input[0], input[1], input[2], input[3], input[3])
        assertEquals(0, h.auth.registerCalls)
        assertEquals(AuthProblem.INVALID_INPUT, h.session.ui.value.problem)
    }

    @Test fun passwordMismatchDoesNotCreateAccount() {
        val h = harness()
        h.session.register("Student", "12345", "student@example.test", "secret1", "secret2")
        assertEquals(AuthProblem.PASSWORD_MISMATCH, h.session.ui.value.problem)
        assertEquals(0, h.auth.registerCalls)
    }

    @Test fun registrationCannotBeSubmittedTwiceWhilePending() {
        val h = harness()
        val hold = CompletableDeferred<Unit>()
        h.auth.hold = hold
        repeat(2) { h.session.register("Student", "12345", "student@example.test", "secret1", "secret1") }
        assertEquals(1, h.auth.registerCalls)
        assertTrue(h.session.ui.value.busy)
        hold.complete(Unit)
        assertFalse(h.session.ui.value.busy)
        assertTrue(h.session.ui.value.session is SessionState.EmailVerificationRequired)
    }

    @Test fun failedVerificationPreservesCreatedIdentityAndDoesNotCreateAnotherAccount() {
        val h = harness()
        h.auth.verificationProblem = AuthProblem.NETWORK
        h.session.register("Student", "12345", "student@example.test", "secret1", "secret1")
        assertNotNull(h.auth.user)
        assertTrue(h.session.ui.value.session is SessionState.EmailVerificationRequired)
        assertEquals(AuthProblem.NETWORK, h.session.ui.value.problem)
        h.session.register("Student", "12345", "student@example.test", "secret1", "secret1")
        assertEquals(1, h.auth.registerCalls)
    }

    @Test fun loginDoesNotTreatIdentityAsAuthorization() {
        val h = harness()
        h.session.login("student@example.test", "secret1")
        assertTrue(h.session.ui.value.session is SessionState.EmailVerificationRequired)
        assertFalse(SessionPolicy.canOpenLiveRole(h.session.ui.value.session, UserRole.STUDENT))
    }

    @Test fun invalidLoginDoesNotCallRepository() {
        val h = harness()
        h.session.login("invalid", "short")
        assertEquals(0, h.auth.loginCalls)
    }

    @Test fun duplicateLoginIsPrevented() {
        val h = harness()
        val hold = CompletableDeferred<Unit>()
        h.auth.hold = hold
        repeat(2) { h.session.login("student@example.test", "secret1") }
        assertEquals(1, h.auth.loginCalls)
        hold.complete(Unit)
    }

    @Test fun loginFailuresAreSafeRecoverableStates() {
        for (problem in listOf(AuthProblem.INVALID_CREDENTIALS, AuthProblem.ACCOUNT_UNAVAILABLE,
            AuthProblem.NETWORK, AuthProblem.THROTTLED, AuthProblem.PROVIDER_UNAVAILABLE)) {
            val h = harness()
            h.auth.nextProblem = problem
            h.session.login("student@example.test", "secret1")
            assertEquals(SessionState.RecoverableError(problem), h.session.ui.value.session)
            assertFalse(h.session.ui.value.busy)
        }
    }

    @Test fun signOutClearsIdentityAndInvalidatesNavigationEpoch() {
        val h = harness(unverified)
        val epoch = h.session.ui.value.navigationEpoch
        h.session.signOut()
        assertNull(h.auth.user)
        assertEquals(SessionState.SignedOut, h.session.ui.value.session)
        assertTrue(h.session.ui.value.navigationEpoch > epoch)
    }

    @Test fun lateLoginAfterLogoutCannotRestoreAccess() {
        val h = harness()
        val hold = CompletableDeferred<Unit>()
        h.auth.hold = hold
        h.session.login("student@example.test", "secret1")
        h.session.signOut()
        hold.complete(Unit)
        assertNull(h.auth.user)
        assertEquals(SessionState.SignedOut, h.session.ui.value.session)
    }

    @Test fun recheckReloadsVerificationAndForcesTokenRefreshOnlyOnce() {
        val h = harness(unverified)
        h.auth.serverVerified = true
        h.session.recheck()
        assertTrue(h.session.ui.value.session is SessionState.ProvisioningPending)
        assertEquals(1, h.auth.reloadCalls)
        assertEquals(1, h.auth.forcedRefreshes)
    }

    @Test fun resendVerificationUsesCooldownAndCanRetryLater() {
        val h = harness(unverified)
        h.session.resendVerification()
        h.session.resendVerification()
        assertEquals(1, h.auth.verificationCalls)
        h.advance(SessionCoordinator.RESEND_COOLDOWN)
        h.session.resendVerification()
        assertEquals(2, h.auth.verificationCalls)
    }

    @Test fun passwordRecoveryConfirmsGenerically() {
        val h = harness()
        h.session.recoverPassword("student@example.test")
        assertEquals(1, h.auth.recoveryCalls)
        assertEquals(AuthNotice.RECOVERY_REQUESTED, h.session.ui.value.notice)
        assertEquals(SessionState.SignedOut, h.session.ui.value.session)
    }

    @Test fun passwordRecoveryFailureDoesNotReportSuccess() {
        val h = harness()
        h.auth.nextProblem = AuthProblem.NETWORK
        h.session.recoverPassword("student@example.test")
        assertNull(h.session.ui.value.notice)
        assertEquals(AuthProblem.NETWORK, h.session.ui.value.problem)
    }

    @Test fun changedTokenInvalidatesPreviouslyAuthorizedSession() {
        val user = verified(UserRole.STAFF, 2)
        val h = harness(user, ProfileVerification.Verified(TrustedProfile(user.uid, UserRole.STAFF, 2, true)))
        val epoch = h.session.ui.value.navigationEpoch
        h.auth.emit(user.copy(claims = AuthorizationClaims(UserRole.STAFF, 3)))
        assertTrue(h.session.ui.value.session is SessionState.ProvisioningPending)
        assertTrue(h.session.ui.value.navigationEpoch > epoch)
    }

    @Test fun lateSessionCallbacksDoNotChangeDemoMode() {
        val h = harness()
        h.session.enterDemo(DemoEntry.ADMIN)
        h.auth.emit(verified())
        assertEquals(ApplicationMode.Demo(DemoEntry.ADMIN), h.session.mode.value)
        assertFalse(SessionPolicy.canOpenLiveRole(h.session.ui.value.session, UserRole.ADMIN))
        h.session.returnToAuthentication()
        assertEquals(ApplicationMode.Authentication, h.session.mode.value)
    }

    @Test fun closingCoordinatorUnregistersItsObservation() {
        val h = harness()
        assertEquals(1, h.auth.observers)
        h.session.close()
        assertEquals(0, h.auth.observers)
    }

    @Test fun genericEmailValidationDoesNotInventInstitutionalDomainPolicy() {
        assertNull(AuthValidation.registration("Student", "12345", "person@another.example", "secret1", "secret1"))
    }

    @Test fun errorMappingNeverIncludesRawServerMessages() {
        assertEquals(AuthProblem.INVALID_CREDENTIALS, AuthProblem.fromCode("ERROR_INVALID_CREDENTIAL"))
        assertEquals(AuthProblem.REGISTRATION_FAILED, AuthProblem.fromCode("ERROR_EMAIL_ALREADY_IN_USE"))
        assertEquals(AuthProblem.ACCOUNT_UNAVAILABLE, AuthProblem.fromCode("ERROR_USER_DISABLED"))
        assertEquals(AuthProblem.PROVIDER_UNAVAILABLE, AuthProblem.fromCode("ERROR_CONFIGURATION_NOT_FOUND"))
        assertEquals(AuthProblem.WEAK_PASSWORD, AuthProblem.fromCode("ERROR_WEAK_PASSWORD"))
        assertEquals(AuthProblem.NETWORK, AuthProblem.fromCode(null, network = true))
        assertEquals(AuthProblem.THROTTLED, AuthProblem.fromCode(null, throttled = true))
        assertEquals("UNKNOWN", AuthFailure(AuthProblem.fromCode("sensitive-raw-message")).message)
    }

    @Test fun missingConfigurationRemainsBuildableAndUnavailable() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val session = SessionCoordinator(UnavailableAuthRepository(), ClosedUserProfileRepository(), scope)
        cleanups += { session.close(); scope.cancel() }
        session.login("student@example.test", "secret1")
        assertEquals(SessionState.RecoverableError(AuthProblem.PROVIDER_UNAVAILABLE), session.ui.value.session)
    }

    private class FakeAuth(initialUser: AuthenticatedUser?) : AuthRepository {
        private val ticks = MutableStateFlow(0)
        var observers = 0
        override val changes = ticks.map { Unit }.onStart { observers++ }.onCompletion { observers-- }
        var user = initialUser
        var registerCalls = 0
        var loginCalls = 0
        var verificationCalls = 0
        var recoveryCalls = 0
        var reloadCalls = 0
        var forcedRefreshes = 0
        var lastEmail: String? = null
        var hold: CompletableDeferred<Unit>? = null
        var nextProblem: AuthProblem? = null
        var verificationProblem: AuthProblem? = null
        var serverVerified = false
        private fun failIfRequested() {
            nextProblem?.let { nextProblem = null; throw AuthFailure(it) }
        }
        fun emit(value: AuthenticatedUser?) { user = value; ticks.value++ }
        override suspend fun currentUser(forceRefresh: Boolean): AuthenticatedUser? {
            if (forceRefresh) { forcedRefreshes++; ticks.value++ }
            return user
        }
        override suspend fun register(fullName: String, email: String, password: String) {
            registerCalls++; lastEmail = email; failIfRequested(); hold?.await()
            emit(AuthenticatedUser("auth-uid", email, fullName, false))
        }
        override suspend fun login(email: String, password: String) {
            loginCalls++; lastEmail = email; failIfRequested(); hold?.await()
            emit(AuthenticatedUser("auth-uid", email, "Student", false))
        }
        override suspend fun reload() { reloadCalls++; emit(user?.copy(emailVerified = serverVerified)) }
        override suspend fun sendVerification() {
            verificationCalls++
            verificationProblem?.let { throw AuthFailure(it) }
        }
        override suspend fun recoverPassword(email: String) { recoveryCalls++; failIfRequested() }
        override fun signOut() { if (user != null) emit(null) }
    }
}
