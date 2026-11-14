package com.example.campusq.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campusq.R
import com.example.campusq.data.auth.ApplicationMode
import com.example.campusq.data.auth.AuthNotice
import com.example.campusq.data.auth.AuthProblem
import com.example.campusq.data.auth.AuthUiState
import com.example.campusq.data.auth.AuthenticatedUser
import com.example.campusq.data.auth.DemoEntry
import com.example.campusq.data.auth.SessionState
import com.example.campusq.data.auth.userOrNull
import com.example.campusq.ui.auth.LoginScreen
import com.example.campusq.ui.auth.RegistrationScreen
import com.example.campusq.ui.auth.SplashScreen
import com.example.campusq.ui.components.CampusQFullLogo
import com.example.campusq.ui.components.CampusQPrimaryButton
import com.example.campusq.ui.components.CampusQSecondaryButton
import com.example.campusq.ui.theme.CampusQTheme
import com.example.campusq.viewmodel.AuthViewModel
import com.example.campusq.viewmodel.CampusQViewModel
import kotlinx.coroutines.delay

@Composable
fun CampusQApp(
    modifier: Modifier = Modifier,
    viewModel: CampusQViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    authViewModel: AuthViewModel? = null
) {
    val context = LocalContext.current.applicationContext
    val factory = remember(context) { AuthViewModel.Factory(context) }
    val auth = authViewModel ?: androidx.lifecycle.viewmodel.compose.viewModel<AuthViewModel>(factory = factory)
    val ui by auth.ui.collectAsStateWithLifecycle()
    val mode by auth.mode.collectAsStateWithLifecycle()
    when (val selected = mode) {
        is ApplicationMode.Demo -> key(selected) {
            val route = when (selected.entry) {
                DemoEntry.HUB -> CampusQRoutes.DEMO_PREVIEW_HUB
                DemoEntry.STUDENT -> CampusQRoutes.STUDENT_HOME
                DemoEntry.STAFF -> CampusQRoutes.STAFF_DASHBOARD
                DemoEntry.ADMIN -> CampusQRoutes.ADMIN_DASHBOARD
            }
            CampusQDemoApp(viewModel = viewModel, modifier = modifier, startRoute = route, onReturnToAuthentication = auth::returnToAuthentication)
        }
        ApplicationMode.Authentication -> key("authentication", ui.navigationEpoch) {
            // This graph intentionally has no live role destinations or demonstration repository data.
            if (ui.session.userOrNull() != null || ui.session == SessionState.Initializing) {
                AuthSessionPanel(ui = ui, modifier = modifier,
                    onResend = auth::resendVerification, onRecheck = auth::recheck,
                    onSignOut = auth::signOut, onDemo = { auth.enterDemo(DemoEntry.HUB) })
            } else {
                val nav = rememberNavController()
                val message = authMessage(ui)
                NavHost(navController = nav, startDestination = CampusQRoutes.SPLASH, modifier = modifier) {
                    composable(CampusQRoutes.SPLASH) {
                        SplashScreen(onNavigateToLogin = {
                            nav.navigate(CampusQRoutes.LOGIN) {
                                popUpTo(CampusQRoutes.SPLASH) { inclusive = true }
                                launchSingleTop = true
                            }
                        }, onNavigateToDemoHub = { auth.enterDemo(DemoEntry.HUB) })
                    }
                    composable(CampusQRoutes.LOGIN) {
                        LoginScreen(
                            onNavigateToRegister = { nav.navigate(CampusQRoutes.REGISTER) { launchSingleTop = true } },
                            onNavigateToDemoHub = { auth.enterDemo(DemoEntry.HUB) },
                            onOpenStudentDemo = { auth.enterDemo(DemoEntry.STUDENT) },
                            onOpenStaffDemo = { auth.enterDemo(DemoEntry.STAFF) },
                            onOpenAdminDemo = { auth.enterDemo(DemoEntry.ADMIN) },
                            onLogin = auth::login, onPasswordRecovery = auth::recoverPassword,
                            isSubmitting = ui.busy, authenticationMessage = message
                        )
                    }
                    composable(CampusQRoutes.REGISTER) {
                        RegistrationScreen(
                            onNavigateBackToLogin = { nav.popBackStack() },
                            onNavigateToDemoHub = { auth.enterDemo(DemoEntry.HUB) },
                            onRegister = { name, id, email, password ->
                                auth.register(name, id, email, password, password)
                            }, isSubmitting = ui.busy, authenticationMessage = message
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun authMessage(ui: AuthUiState): String? {
    val problem = ui.problem
    if (problem != null) return stringResource(when (problem) {
        AuthProblem.INVALID_INPUT -> R.string.auth_error_input
        AuthProblem.PASSWORD_MISMATCH -> R.string.auth_error_match
        AuthProblem.INVALID_CREDENTIALS -> R.string.auth_error_credentials
        AuthProblem.ACCOUNT_UNAVAILABLE -> R.string.auth_error_account
        AuthProblem.NETWORK -> R.string.auth_error_network
        AuthProblem.THROTTLED -> R.string.auth_error_throttled
        AuthProblem.PROVIDER_UNAVAILABLE -> R.string.auth_error_provider
        AuthProblem.WEAK_PASSWORD -> R.string.auth_error_password
        AuthProblem.REGISTRATION_FAILED -> R.string.auth_error_registration
        AuthProblem.VERIFICATION_FAILED -> R.string.auth_error_verification
        AuthProblem.UNKNOWN -> R.string.auth_error_unknown
    })
    return ui.notice?.let { stringResource(when (it) {
        AuthNotice.RECOVERY_REQUESTED -> R.string.auth_recovery_notice
        AuthNotice.VERIFICATION_SENT -> R.string.auth_verification_notice
        AuthNotice.ACCOUNT_CREATED -> R.string.auth_account_notice
    }) }
}

@Composable
fun AuthSessionPanel(
    ui: AuthUiState,
    modifier: Modifier = Modifier,
    onResend: () -> Unit = {},
    onRecheck: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onDemo: () -> Unit = {}
) {
    val session = ui.session
    val now by produceState(System.currentTimeMillis(), ui.resendAvailableAt) {
        value = System.currentTimeMillis()
        while (value < ui.resendAvailableAt) {
            delay(1_000)
            value = System.currentTimeMillis()
        }
    }
    val remaining = ((ui.resendAvailableAt - now).coerceAtLeast(0) + 999) / 1000
    Column(
        modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CampusQFullLogo(iconSize = 72.dp)
        Text(stringResource(when (session) {
            is SessionState.EmailVerificationRequired -> R.string.auth_verify_title
            is SessionState.ProvisioningPending -> R.string.auth_pending_title
            is SessionState.ProfileUnavailable -> R.string.auth_profile_title
            is SessionState.Authorized -> R.string.auth_live_title
            else -> R.string.auth_initializing
        }), style = MaterialTheme.typography.headlineSmall)
        session.userOrNull()?.email?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        Text(stringResource(when (session) {
            is SessionState.EmailVerificationRequired -> R.string.auth_verify_body
            is SessionState.ProvisioningPending -> R.string.auth_pending_body
            is SessionState.ProfileUnavailable -> R.string.auth_profile_body
            else -> R.string.auth_live_body
        }), style = MaterialTheme.typography.bodyMedium)
        authMessage(ui)?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        if (session is SessionState.EmailVerificationRequired) {
            CampusQPrimaryButton(
                text = if (remaining > 0) pluralStringResource(R.plurals.auth_resend_wait, remaining.toInt(), remaining.toInt())
                    else stringResource(R.string.auth_resend),
                onClick = onResend, enabled = !ui.busy && remaining == 0L, isLoading = ui.busy
            )
        }
        if (session.userOrNull() != null) {
            CampusQSecondaryButton(text = stringResource(R.string.auth_recheck), onClick = onRecheck, enabled = !ui.busy)
        }
        CampusQSecondaryButton(text = stringResource(R.string.auth_sign_out), onClick = onSignOut)
        CampusQSecondaryButton(text = stringResource(R.string.auth_demo), onClick = onDemo)
    }
}

@Preview(showBackground = true)
@Composable
private fun VerificationPanelPreview() {
    CampusQTheme {
        AuthSessionPanel(AuthUiState(SessionState.EmailVerificationRequired(
            AuthenticatedUser("preview", "student@example.test", "Preview Student", false))))
    }
}

/** Compatibility for existing demo workflow tests and hosts. */
@Composable
fun CampusQApp(viewModel: CampusQViewModel) {
    CampusQApp(modifier = Modifier, viewModel = viewModel)
}
