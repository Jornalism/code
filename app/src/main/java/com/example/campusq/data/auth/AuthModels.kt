package com.example.campusq.data.auth

import com.example.campusq.data.model.UserRole

data class AuthenticatedUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val emailVerified: Boolean,
    val claims: AuthorizationClaims = AuthorizationClaims()
)

data class AuthorizationClaims(
    val role: UserRole? = null,
    val version: Long? = null,
    val malformed: Boolean = false
) {
    companion object {
        fun fromToken(claims: Map<String, Any?>): AuthorizationClaims {
            val rawRole = claims["role"]
            val rawVersion = claims["authorizationVersion"]
            val role = (rawRole as? String)?.let { value -> UserRole.entries.find { it.name == value } }
            val version = when (rawVersion) {
                is Long -> rawVersion
                is Int -> rawVersion.toLong()
                else -> null
            }
            return AuthorizationClaims(role, version,
                (rawRole != null && role == null) || (rawVersion != null && (version == null || version < 1)))
        }
    }
}

enum class AuthOperation { RESTORE, REGISTER, LOGIN, VERIFY_EMAIL, RECHECK, RECOVER_PASSWORD }
enum class AuthProblem {
    INVALID_INPUT, PASSWORD_MISMATCH, INVALID_CREDENTIALS, ACCOUNT_UNAVAILABLE,
    NETWORK, THROTTLED, PROVIDER_UNAVAILABLE, WEAK_PASSWORD, REGISTRATION_FAILED,
    VERIFICATION_FAILED, UNKNOWN;

    companion object {
        fun fromCode(code: String?, network: Boolean = false, throttled: Boolean = false): AuthProblem = when {
            network -> NETWORK
            throttled -> THROTTLED
            else -> when (code) {
                "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND",
                "ERROR_INVALID_LOGIN_CREDENTIALS" -> INVALID_CREDENTIALS
                "ERROR_USER_DISABLED" -> ACCOUNT_UNAVAILABLE
                "ERROR_NETWORK_REQUEST_FAILED" -> NETWORK
                "ERROR_TOO_MANY_REQUESTS", "ERROR_QUOTA_EXCEEDED" -> THROTTLED
                "ERROR_OPERATION_NOT_ALLOWED", "ERROR_CONFIGURATION_NOT_FOUND",
                "ERROR_PROJECT_NOT_FOUND", "ERROR_API_NOT_AVAILABLE" -> PROVIDER_UNAVAILABLE
                "ERROR_WEAK_PASSWORD" -> WEAK_PASSWORD
                "ERROR_EMAIL_ALREADY_IN_USE" -> REGISTRATION_FAILED
                "ERROR_INVALID_EMAIL" -> INVALID_INPUT
                else -> UNKNOWN
            }
        }
    }
}

class AuthFailure(val problem: AuthProblem) : Exception(problem.name)
enum class AuthNotice { VERIFICATION_SENT, RECOVERY_REQUESTED, ACCOUNT_CREATED }
enum class PendingReason { NO_TRUSTED_ROLE, INVALID_CLAIMS, INCONSISTENT_PROFILE }

sealed interface SessionState {
    data object Initializing : SessionState
    data object SignedOut : SessionState
    data class Submitting(val operation: AuthOperation, val user: AuthenticatedUser?) : SessionState
    data class EmailVerificationRequired(val user: AuthenticatedUser) : SessionState
    data class ProvisioningPending(val user: AuthenticatedUser, val reason: PendingReason) : SessionState
    data class ProfileUnavailable(val user: AuthenticatedUser) : SessionState
    data class Authorized(val user: AuthenticatedUser, val role: UserRole) : SessionState
    data class RecoverableError(val problem: AuthProblem, val user: AuthenticatedUser? = null) : SessionState
}

fun SessionState.userOrNull(): AuthenticatedUser? = when (this) {
    is SessionState.Submitting -> user
    is SessionState.EmailVerificationRequired -> user
    is SessionState.ProvisioningPending -> user
    is SessionState.ProfileUnavailable -> user
    is SessionState.Authorized -> user
    is SessionState.RecoverableError -> user
    else -> null
}

data class AuthUiState(
    val session: SessionState = SessionState.Initializing,
    val busy: Boolean = false,
    val problem: AuthProblem? = null,
    val notice: AuthNotice? = null,
    val resendAvailableAt: Long = 0,
    val navigationEpoch: Long = 0
)

enum class DemoEntry { HUB, STUDENT, STAFF, ADMIN }
sealed interface ApplicationMode {
    data object Authentication : ApplicationMode
    data class Demo(val entry: DemoEntry) : ApplicationMode
}

object AuthValidation {
    private val email = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    fun login(emailAddress: String, password: String): AuthProblem? =
        if (!email.matches(emailAddress.trim()) || password.length < 6) AuthProblem.INVALID_INPUT else null

    fun registration(name: String, studentNumber: String, emailAddress: String,
                     password: String, confirmation: String): AuthProblem? = when {
        name.trim().length < 3 || studentNumber.trim().length < 5 -> AuthProblem.INVALID_INPUT
        login(emailAddress, password) != null -> AuthProblem.INVALID_INPUT
        password != confirmation -> AuthProblem.PASSWORD_MISMATCH
        else -> null
    }
}
