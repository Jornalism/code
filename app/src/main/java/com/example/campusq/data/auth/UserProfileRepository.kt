package com.example.campusq.data.auth

import com.example.campusq.data.model.UserRole

/** Future implementation must verify trusted server-owned users/{uid} and role profiles. */
interface UserProfileRepository {
    suspend fun verify(user: AuthenticatedUser): ProfileVerification
}

data class TrustedProfile(
    val uid: String,
    val role: UserRole,
    val authorizationVersion: Long,
    val active: Boolean
)

sealed interface ProfileVerification {
    data object Unavailable : ProfileVerification
    data object Pending : ProfileVerification
    data class Verified(val profile: TrustedProfile) : ProfileVerification
}

/** Published rules deny access. Never queries, writes or fabricates a trusted profile. */
class ClosedUserProfileRepository : UserProfileRepository {
    override suspend fun verify(user: AuthenticatedUser) = ProfileVerification.Unavailable
}

object SessionPolicy {
    fun evaluate(user: AuthenticatedUser, profile: ProfileVerification): SessionState {
        if (!user.emailVerified) return SessionState.EmailVerificationRequired(user)
        val claims = user.claims
        if (claims.malformed) return SessionState.ProvisioningPending(user, PendingReason.INVALID_CLAIMS)
        val role = claims.role
        val version = claims.version
        if (role == null || version == null || version < 1) {
            return SessionState.ProvisioningPending(user, PendingReason.NO_TRUSTED_ROLE)
        }
        return when (profile) {
            ProfileVerification.Unavailable -> SessionState.ProfileUnavailable(user)
            ProfileVerification.Pending -> SessionState.ProvisioningPending(user, PendingReason.INCONSISTENT_PROFILE)
            is ProfileVerification.Verified -> {
                val trusted = profile.profile
                if (trusted.uid != user.uid || trusted.role != role ||
                    trusted.authorizationVersion != version || !trusted.active) {
                    SessionState.ProvisioningPending(user, PendingReason.INCONSISTENT_PROFILE)
                } else SessionState.Authorized(user, role)
            }
        }
    }

    fun canOpenLiveRole(state: SessionState, requiredRole: UserRole): Boolean =
        state is SessionState.Authorized && state.user.emailVerified && state.role == requiredRole
}
