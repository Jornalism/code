package com.example.campusq.data.auth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Owns no passwords/tokens. Firebase owns persistence; demo mode never supplies authority. */
class SessionCoordinator(
    private val auth: AuthRepository,
    private val profiles: UserProfileRepository,
    private val scope: CoroutineScope,
    private val now: () -> Long = System::currentTimeMillis
) {
    private val mutableUi = MutableStateFlow(AuthUiState())
    val ui = mutableUi.asStateFlow()
    private val mutableMode = MutableStateFlow<ApplicationMode>(ApplicationMode.Authentication)
    val mode = mutableMode.asStateFlow()
    private var pendingRefresh = false
    private var logoutRequested = false
    private var closed = false
    private var operationJob: Job? = null
    private val listenerJob = scope.launch {
        auth.changes.collect {
            if (mutableUi.value.busy) pendingRefresh = true else refresh()
        }
    }

    fun enterDemo(entry: DemoEntry) { mutableMode.value = ApplicationMode.Demo(entry) }
    fun returnToAuthentication() { mutableMode.value = ApplicationMode.Authentication }

    fun login(email: String, password: String) {
        val invalid = AuthValidation.login(email, password)
        if (invalid != null) { reject(invalid); return }
        submit(AuthOperation.LOGIN) { auth.login(email.trim(), password) }
    }

    fun register(name: String, studentNumber: String, email: String, password: String, confirmation: String) {
        if (ui.value.session.userOrNull() != null) return
        val invalid = AuthValidation.registration(name, studentNumber, email, password, confirmation)
        if (invalid != null) { reject(invalid); return }
        // The student number is validated as form input only; never stored or turned into a role.
        submit(AuthOperation.REGISTER, AuthNotice.ACCOUNT_CREATED) {
            auth.register(name.trim(), email.trim(), password)
            auth.sendVerification()
            mutableUi.value = mutableUi.value.copy(resendAvailableAt = now() + RESEND_COOLDOWN)
        }
    }

    fun resendVerification() {
        val user = ui.value.session.userOrNull() ?: return
        if (now() < mutableUi.value.resendAvailableAt || user.emailVerified) return
        submit(AuthOperation.VERIFY_EMAIL, AuthNotice.VERIFICATION_SENT) {
            mutableUi.value = mutableUi.value.copy(resendAvailableAt = now() + RESEND_COOLDOWN)
            auth.sendVerification()
        }
    }

    fun recheck() = submit(AuthOperation.RECHECK) { auth.reload() }

    fun recoverPassword(email: String) {
        if (AuthValidation.login(email, "unused-password") != null) {
            reject(AuthProblem.INVALID_INPUT); return
        }
        submit(AuthOperation.RECOVER_PASSWORD, AuthNotice.RECOVERY_REQUESTED) {
            auth.recoverPassword(email.trim())
        }
    }

    fun signOut() {
        logoutRequested = true
        auth.signOut()
        publish(SessionState.SignedOut, problem = null, notice = null)
        // A Firebase Task already in flight may still finish. Its finally block signs out again.
        if (!ui.value.busy) logoutRequested = false
    }

    private fun refresh() = submit(AuthOperation.RESTORE) { }

    private fun reject(problem: AuthProblem) {
        if (!ui.value.busy) {
            mutableUi.value = ui.value.copy(problem = problem, notice = null)
        }
    }

    private fun publish(state: SessionState, problem: AuthProblem?, notice: AuthNotice?) {
        val old = mutableUi.value
        fun key(value: SessionState): String =
            value.userOrNull()?.uid.orEmpty() + ":" + ((value as? SessionState.Authorized)?.role?.name ?: "closed")
        mutableUi.value = old.copy(session = state, problem = problem, notice = notice,
            navigationEpoch = old.navigationEpoch + if (key(old.session) != key(state)) 1 else 0)
    }

    private fun submit(operation: AuthOperation, notice: AuthNotice? = null, action: suspend () -> Unit) {
        if (closed || mutableUi.value.busy) return
        operationJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            mutableUi.value = ui.value.copy(busy = true, problem = null, notice = null)
            publish(SessionState.Submitting(operation, ui.value.session.userOrNull()), null, null)
            var problem: AuthProblem? = null
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: AuthFailure) {
                problem = failure.problem
            } catch (_: Exception) {
                problem = AuthProblem.UNKNOWN
            } finally {
                if (!closed) {
                    var resolved: SessionState
                    var forceRefresh = operation == AuthOperation.RECHECK
                    var signOutAfterTask = logoutRequested
                    do {
                        pendingRefresh = false
                        if (signOutAfterTask) { auth.signOut(); signOutAfterTask = false }
                        resolved = try {
                            val user = auth.currentUser(forceRefresh && !logoutRequested).also { forceRefresh = false }
                            if (user == null) SessionState.SignedOut
                            else if (!user.emailVerified || user.claims.role == null ||
                                user.claims.version == null || user.claims.malformed) {
                                SessionPolicy.evaluate(user, ProfileVerification.Pending)
                            } else {
                                val profile = try { profiles.verify(user) }
                                catch (cancelled: CancellationException) { throw cancelled }
                                catch (_: Exception) { ProfileVerification.Unavailable }
                                SessionPolicy.evaluate(user, profile)
                            }
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (failure: AuthFailure) {
                            problem = failure.problem
                            SessionState.RecoverableError(failure.problem)
                        } catch (_: Exception) {
                            problem = AuthProblem.PROVIDER_UNAVAILABLE
                            SessionState.RecoverableError(AuthProblem.PROVIDER_UNAVAILABLE)
                        }
                    } while (pendingRefresh && !closed)
                    if (problem != null && resolved == SessionState.SignedOut) {
                        resolved = SessionState.RecoverableError(problem)
                    }
                    publish(resolved, problem, if (problem == null) notice else null)
                    mutableUi.value = ui.value.copy(busy = false)
                    logoutRequested = false
                }
            }
        }
    }

    fun close() {
        closed = true
        listenerJob.cancel()
        operationJob?.cancel()
    }

    companion object { const val RESEND_COOLDOWN = 60_000L }
}
