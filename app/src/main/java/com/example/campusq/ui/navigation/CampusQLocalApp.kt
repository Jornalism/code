package com.example.campusq.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campusq.data.local.LocalCampusQRepository
import com.example.campusq.data.model.UserRole
import com.example.campusq.ui.auth.LoginScreen
import com.example.campusq.ui.auth.RegistrationScreen
import com.example.campusq.ui.auth.SplashScreen
import com.example.campusq.ui.components.CampusQFullLogo
import com.example.campusq.ui.components.CampusQSecondaryButton
import com.example.campusq.viewmodel.CampusQViewModel
import com.example.campusq.viewmodel.LocalSessionViewModel
import kotlinx.coroutines.delay

@Composable
fun CampusQLocalApp(modifier: Modifier = Modifier, session: LocalSessionViewModel? = null) {
    val context=LocalContext.current.applicationContext
    val factory=remember(context){LocalSessionViewModel.Factory(context)}
    val auth=session ?: androidx.lifecycle.viewmodel.compose.viewModel<LocalSessionViewModel>(factory=factory)
    val state by auth.ui.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalServerMode provides true) {
        key(state.user?.id, state.epoch) {
            val user=state.user
            if(state.initializing) {
                LocalConnectionPanel("Checking the CampusQ session…", modifier=modifier)
            } else if(user != null) {
                val workflowFactory=remember(auth,user.id) {
                    object:ViewModelProvider.Factory {
                        override fun <T:ViewModel> create(modelClass:Class<T>):T =
                            modelClass.cast(CampusQViewModel(remote=LocalCampusQRepository(auth.api)))!!
                    }
                }
                val workflowStore=remember { ViewModelStore() }
                val workflowOwner=remember { object:ViewModelStoreOwner { override val viewModelStore=workflowStore } }
                DisposableEffect(workflowStore) { onDispose { workflowStore.clear() } }
                val workflow=androidx.lifecycle.viewmodel.compose.viewModel<CampusQViewModel>(
                    viewModelStoreOwner=workflowOwner, key="local-"+user.id+"-"+state.epoch,factory=workflowFactory)
                val ready by workflow.liveReady.collectAsStateWithLifecycle()
                val issue by workflow.connectionIssue.collectAsStateWithLifecycle()
                val busy by workflow.networkBusy.collectAsStateWithLifecycle()
                val expired by workflow.liveSessionExpired.collectAsStateWithLifecycle()
                val lifecycle=LocalLifecycleOwner.current.lifecycle
                var showOverview by remember { mutableStateOf(false) }
                LaunchedEffect(workflow,lifecycle) {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        while(true){ workflow.synchronizeLive();delay(4_000) }
                    }
                }
                LaunchedEffect(expired) { if(expired)auth.expired() }
                Column(modifier=modifier.fillMaxSize()) {
                    if(issue != null) Surface(color=MaterialTheme.colorScheme.errorContainer) {
                        Text(requireNotNull(issue),modifier=Modifier.fillMaxWidth().padding(12.dp),style=MaterialTheme.typography.bodySmall)
                    }
                    Box(Modifier.weight(1f)) {
                        if(!ready) {
                            LocalConnectionPanel("Waiting for the laptop server. Check the local network.",onSignOut=auth::signOut)
                        } else {
                            CompositionLocalProvider(LocalAdminOverview provides { showOverview = true }) {
                            CampusQWorkflowApp(workflow,startRoute=when(user.role){
                                UserRole.STUDENT->CampusQRoutes.STUDENT_HOME
                                UserRole.STAFF->CampusQRoutes.STAFF_DASHBOARD
                                UserRole.ADMIN->CampusQRoutes.ADMIN_DASHBOARD
                            },liveRole=user.role,onReturnToAuthentication=auth::signOut)
                            }
                        }
                        if(busy) Surface(modifier=Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.surface.copy(alpha=0.8f)) {
                            Box(contentAlignment=Alignment.Center){CircularProgressIndicator()}
                        }
                    }
                }
                if(showOverview) AlertDialog(onDismissRequest={showOverview=false},title={Text("Accounts and system activity")},
                    text={Text(workflow.adminOverview(),modifier=Modifier.verticalScroll(rememberScrollState()))},
                    confirmButton={TextButton(onClick={showOverview=false}){Text("Close")}})
            } else if(auth.hasUnconfirmedSavedSession) {
                LocalConnectionPanel(state.message ?: "Waiting to verify the saved session.", modifier=modifier,
                    onSignOut=auth::signOut, onRetry=auth::restore)
            } else {
                val nav=rememberNavController()
                LaunchedEffect(state.registrationSucceeded) {
                    if (state.registrationSucceeded) {
                        nav.navigate(CampusQRoutes.LOGIN) {
                            popUpTo(CampusQRoutes.REGISTER) { inclusive=true }
                            launchSingleTop=true
                        }
                        auth.registrationHandled()
                    }
                }
                NavHost(navController=nav,startDestination=CampusQRoutes.SPLASH,modifier=modifier) {
                    composable(CampusQRoutes.SPLASH) {
                        SplashScreen(onNavigateToLogin={nav.navigate(CampusQRoutes.LOGIN){popUpTo(CampusQRoutes.SPLASH){inclusive=true};launchSingleTop=true}},
                            onNavigateToDemoHub={nav.navigate(CampusQRoutes.LOGIN){launchSingleTop=true}})
                    }
                    composable(CampusQRoutes.LOGIN) {
                        LoginScreen(onNavigateToRegister={nav.navigate(CampusQRoutes.REGISTER){launchSingleTop=true}},
                            onNavigateToDemoHub={},onOpenStudentDemo={},onOpenStaffDemo={},onOpenAdminDemo={},
                            onLogin=auth::login,onPasswordRecovery=auth::recover,isSubmitting=state.busy,authenticationMessage=state.message)
                    }
                    composable(CampusQRoutes.REGISTER) {
                        RegistrationScreen(onNavigateBackToLogin={nav.popBackStack()},onNavigateToDemoHub={},
                            onRegister=auth::register,isSubmitting=state.busy,authenticationMessage=state.message)
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalConnectionPanel(message: String, modifier: Modifier = Modifier, onSignOut: (() -> Unit)? = null, onRetry: (() -> Unit)? = null) {
    Column(modifier=modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
        verticalArrangement=Arrangement.spacedBy(16.dp)) {
        CampusQFullLogo()
        Text(message)
        if(onRetry != null)CampusQSecondaryButton(text="Retry connection",onClick=onRetry)
        if(onSignOut != null)CampusQSecondaryButton(text="Sign out",onClick=onSignOut)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun LocalConnectionPanelPreview() {
    com.example.campusq.ui.theme.CampusQTheme {
        LocalConnectionPanel("Waiting for the laptop server. Check the local network.", onSignOut = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun LocalLoginPreview() {
    com.example.campusq.ui.theme.CampusQTheme {
        CompositionLocalProvider(LocalServerMode provides true) {
            LoginScreen(onNavigateToRegister = {}, onNavigateToDemoHub = {}, onOpenStudentDemo = {},
                onOpenStaffDemo = {}, onOpenAdminDemo = {}, onLogin = { _, _ -> }, onPasswordRecovery = {})
        }
    }
}
