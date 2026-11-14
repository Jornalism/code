package com.example.campusq.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusq.data.auth.AuthRepository
import com.example.campusq.data.auth.ClosedUserProfileRepository
import com.example.campusq.data.auth.DemoEntry
import com.example.campusq.data.auth.FirebaseAuthRepository
import com.example.campusq.data.auth.SessionCoordinator
import com.example.campusq.data.auth.UnavailableAuthRepository
import com.example.campusq.data.auth.UserProfileRepository
import com.example.campusq.data.firebase.FirebaseBackend

class AuthViewModel(
    repository: AuthRepository,
    profiles: UserProfileRepository = ClosedUserProfileRepository()
) : ViewModel() {
    private val session = SessionCoordinator(repository, profiles, viewModelScope)
    val ui = session.ui
    val mode = session.mode
    fun login(email: String, password: String) = session.login(email, password)
    fun register(name: String, studentId: String, email: String, password: String, confirmation: String) =
        session.register(name, studentId, email, password, confirmation)
    fun recoverPassword(email: String) = session.recoverPassword(email)
    fun resendVerification() = session.resendVerification()
    fun recheck() = session.recheck()
    fun signOut() = session.signOut()
    fun enterDemo(entry: DemoEntry) = session.enterDemo(entry)
    fun returnToAuthentication() = session.returnToAuthentication()
    override fun onCleared() { session.close() }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AuthViewModel::class.java))
            val repository = try {
                val backend = FirebaseBackend.fromConfiguration(context, "(default)", "asia-southeast1")
                require(backend.app.options.projectId == "campusq-e06c7")
                // Production provider/account/email operations await separate explicit approval.
                FirebaseAuthRepository(backend.auth)
            } catch (_: IllegalArgumentException) {
                UnavailableAuthRepository()
            } catch (_: IllegalStateException) {
                UnavailableAuthRepository()
            }
            return modelClass.cast(AuthViewModel(repository))!!
        }
    }
}
