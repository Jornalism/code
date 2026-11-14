package com.example.campusq.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusq.BuildConfig
import com.example.campusq.data.auth.AuthValidation
import com.example.campusq.data.local.LocalApi
import com.example.campusq.data.local.LocalApiException
import com.example.campusq.data.local.LocalSessionStore
import com.example.campusq.data.local.jsonBody
import com.example.campusq.data.model.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class LocalIdentity(val id: String, val fullName: String, val email: String, val role: UserRole)
data class LocalSessionUi(val initializing: Boolean = true, val busy: Boolean = false,
    val user: LocalIdentity? = null, val message: String? = null, val epoch: Int = 0,
    val registrationSucceeded: Boolean = false)

class LocalSessionViewModel(private val store: LocalSessionStore, val baseUrl: String) : ViewModel() {
    @Volatile private var credential: String? = null
    val hasUnconfirmedSavedSession: Boolean get() = credential != null && ui.value.user == null
    val api = LocalApi(baseUrl) { credential }
    private val mutableUi = MutableStateFlow(LocalSessionUi())
    val ui = mutableUi.asStateFlow()

    init { restore() }
    private fun identity(json: JSONObject): LocalIdentity {
        val user = json.getJSONObject("user")
        check(user.getString("accountStatus") == "ACTIVE")
        return LocalIdentity(user.getString("id"),user.getString("fullName"),user.getString("email"),UserRole.valueOf(user.getString("role")))
    }
    fun restore() = operation {
        credential = withContext(Dispatchers.IO) { store.read() }
        val user = if (credential == null) null else identity(api.request("GET", "/api/session"))
        mutableUi.value = mutableUi.value.copy(user = user, message = null)
    }
    fun login(email: String, password: String) {
        if (AuthValidation.login(email,password) != null) { message("Check your email and password."); return }
        operation { establish(api.request("POST", "/api/auth/login",jsonBody("email" to email.trim(),"password" to password))) }
    }
    fun register(name: String, studentId: String, email: String, password: String) {
        // Registration is available only while signed out; never replace an existing session.
        if (ui.value.initializing || ui.value.busy || ui.value.user != null || credential != null) return
        if (AuthValidation.registration(name,studentId,email,password,password) != null || password.length < 8) {
            message("Check all fields. Use a password of at least 8 characters."); return
        }
        operation {
            api.request("POST","/api/auth/register",jsonBody("fullName" to name.trim(),"studentId" to studentId.trim(),"email" to email.trim(),"password" to password))
            mutableUi.value=mutableUi.value.copy(
                registrationSucceeded=true,
                message="Registration Successful. Your account has been saved. Please log in with your email and password.")
        }
    }
    fun registrationHandled() {
        mutableUi.value=mutableUi.value.copy(registrationSucceeded=false)
    }
    private suspend fun establish(response: JSONObject) {
        val user = identity(response)
        credential = response.getString("token")
        try { withContext(Dispatchers.IO) { store.save(requireNotNull(credential)) } }
        catch (error: Exception) {
            try { api.request("POST","/api/auth/logout",jsonBody()) } catch (_: Exception) { }
            credential = null
            throw error
        }
        mutableUi.value=mutableUi.value.copy(user=user,epoch=ui.value.epoch+1,message=null)
    }
    fun recover(email: String) = operation {
        val result=api.request("POST","/api/auth/password-recovery",jsonBody("email" to email.trim()))
        mutableUi.value=mutableUi.value.copy(message=result.getString("message"))
    }
    fun signOut() = operation {
        var notice: String? = null
        try { if (credential != null) api.request("POST","/api/auth/logout",jsonBody()) }
        catch (_: Exception) { notice="Signed out on this device. Server revocation could not be confirmed; the server session expires automatically." }
        credential=null
        mutableUi.value=LocalSessionUi(initializing=false,message=notice,epoch=ui.value.epoch+1)
        withContext(Dispatchers.IO) { store.clear() }
    }
    fun expired() {
        credential=null
        viewModelScope.launch(Dispatchers.IO) { runCatching { store.clear() } }
        mutableUi.value=LocalSessionUi(initializing=false,message="Your session ended. Please sign in again.",epoch=ui.value.epoch+1)
    }
    private fun message(value:String) { if(!ui.value.busy) mutableUi.value=ui.value.copy(message=value) }
    private fun operation(block: suspend () -> Unit) {
        if(ui.value.busy)return
        mutableUi.value=ui.value.copy(busy=true,message=null)
        viewModelScope.launch {
            try { block() }
            catch(cancelled:CancellationException){throw cancelled}
            catch(error:LocalApiException){
                if(error.status==401 && credential!=null)expired()
                mutableUi.value=ui.value.copy(message=error.message)
            } catch(_:Exception) { mutableUi.value=ui.value.copy(message="Cannot reach the CampusQ server or save the session. Check the laptop server and local network, then retry.") }
            finally { mutableUi.value=ui.value.copy(busy=false,initializing=false) }
        }
    }
    class Factory(context: Context) : ViewModelProvider.Factory {
        private val context=context.applicationContext
        override fun <T:ViewModel> create(modelClass:Class<T>):T =
            modelClass.cast(LocalSessionViewModel(LocalSessionStore(context,BuildConfig.LOCAL_BACKEND_URL),BuildConfig.LOCAL_BACKEND_URL))!!
    }
}
