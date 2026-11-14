package com.example.campusq.data.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface AuthRepository {
    val changes: Flow<Unit>
    suspend fun currentUser(forceRefresh: Boolean = false): AuthenticatedUser?
    suspend fun register(fullName: String, email: String, password: String)
    suspend fun login(email: String, password: String)
    suspend fun reload()
    suspend fun sendVerification()
    suspend fun recoverPassword(email: String)
    fun signOut()
}

/** Safe for no-config builds. No SDK calls or production account/email operations. */
class UnavailableAuthRepository : AuthRepository {
    override val changes = flowOf(Unit)
    override suspend fun currentUser(forceRefresh: Boolean): AuthenticatedUser? = null
    private fun unavailable(): Nothing = throw AuthFailure(AuthProblem.PROVIDER_UNAVAILABLE)
    override suspend fun register(fullName: String, email: String, password: String) = unavailable()
    override suspend fun login(email: String, password: String) = unavailable()
    override suspend fun reload() = unavailable()
    override suspend fun sendVerification() = unavailable()
    override suspend fun recoverPassword(email: String) = unavailable()
    override fun signOut() = Unit
}
