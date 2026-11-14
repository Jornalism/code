package com.example.campusq.data.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** SDK adapter. Mutation operations are deliberately limited to the explicit local demo emulator. */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val emulatorOperations: Boolean = false
) : AuthRepository {
    init {
        require(!emulatorOperations || auth.app.options.projectId == "demo-campusq-local") {
            "This stage permits Auth operations only in the local demo project."
        }
    }

    override val changes: Flow<Unit> = callbackFlow {
        val authListener = FirebaseAuth.AuthStateListener { trySend(Unit); Unit }
        val tokenListener = FirebaseAuth.IdTokenListener { trySend(Unit); Unit }
        auth.addAuthStateListener(authListener)
        auth.addIdTokenListener(tokenListener)
        trySend(Unit)
        awaitClose {
            auth.removeAuthStateListener(authListener)
            auth.removeIdTokenListener(tokenListener)
        }
    }

    override suspend fun currentUser(forceRefresh: Boolean): AuthenticatedUser? = safe {
        val user = auth.currentUser ?: return@safe null
        val token = user.getIdToken(forceRefresh).awaitAuth()
        AuthenticatedUser(user.uid, user.email, user.displayName, user.isEmailVerified,
            AuthorizationClaims.fromToken(token.claims))
    }

    private fun requireOperations() {
        if (!emulatorOperations) throw AuthFailure(AuthProblem.PROVIDER_UNAVAILABLE)
    }

    override suspend fun register(fullName: String, email: String, password: String) = safe {
        requireOperations()
        if (auth.currentUser != null) throw AuthFailure(AuthProblem.REGISTRATION_FAILED)
        val user = auth.createUserWithEmailAndPassword(email, password).awaitAuth().user
            ?: throw AuthFailure(AuthProblem.REGISTRATION_FAILED)
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(fullName).build()).awaitAuth()
        Unit
    }

    override suspend fun login(email: String, password: String) = safe {
        requireOperations()
        auth.signInWithEmailAndPassword(email, password).awaitAuth()
        Unit
    }

    override suspend fun reload() = safe {
        requireOperations()
        auth.currentUser?.reload()?.awaitAuth()
        Unit
    }

    override suspend fun sendVerification() = safe {
        requireOperations()
        val user = auth.currentUser ?: throw AuthFailure(AuthProblem.VERIFICATION_FAILED)
        user.sendEmailVerification().awaitAuth()
        Unit
    }

    override suspend fun recoverPassword(email: String) = safe {
        requireOperations()
        try {
            auth.sendPasswordResetEmail(email).awaitAuth()
        } catch (error: FirebaseAuthException) {
            if (error.errorCode != "ERROR_USER_NOT_FOUND") throw error
        }
        Unit
    }

    override fun signOut() { auth.signOut() }

    private suspend fun <T> safe(block: suspend () -> T): T = try {
        block()
    } catch (cancelled: kotlinx.coroutines.CancellationException) {
        throw cancelled
    } catch (failure: AuthFailure) {
        throw failure
    } catch (error: Exception) {
        throw AuthFailure(AuthProblem.fromCode((error as? FirebaseAuthException)?.errorCode,
            error is FirebaseNetworkException, error is FirebaseTooManyRequestsException))
    }
}

private suspend fun <T> Task<T>.awaitAuth(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (continuation.isActive) {
            if (task.isSuccessful) continuation.resume(task.result)
            else continuation.resumeWithException(task.exception ?: AuthFailure(AuthProblem.UNKNOWN))
        }
    }
}
