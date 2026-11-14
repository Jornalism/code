package com.example.campusq

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.test.espresso.Espresso.pressBack
import com.example.campusq.data.auth.AuthRepository
import com.example.campusq.data.auth.AuthenticatedUser
import com.example.campusq.data.auth.AuthorizationClaims
import com.example.campusq.data.model.UserRole
import com.example.campusq.ui.auth.LoginScreen
import com.example.campusq.ui.navigation.CampusQApp
import com.example.campusq.ui.theme.CampusQTheme
import com.example.campusq.viewmodel.AuthViewModel
import com.example.campusq.viewmodel.CampusQViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class Phase4AuthUiTest {
    @get:Rule val compose = createComposeRule()

    private fun launch(repo: UiAuth = UiAuth()): AuthViewModel {
        val auth = AuthViewModel(repo)
        val demo = CampusQViewModel()
        compose.setContent { CampusQTheme {
            CampusQApp(modifier = Modifier, viewModel = demo, authViewModel = auth)
        } }
        return auth
    }

    private fun awaitText(text: String) {
        compose.waitUntil(15_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun passwordIsNotRestoredWithSavedInstanceState() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { CampusQTheme { LoginScreen({}, {}, {}, {}, {}) } }
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("student@example.test")
        compose.onAllNodes(hasSetTextAction())[1].performTextInput("local-test-password")
        restoration.emulateSavedInstanceStateRestore()
        compose.onAllNodes(hasSetTextAction())[0].assert(
            SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("student@example.test")))
        compose.onAllNodes(hasSetTextAction())[1].assert(
            SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("")))
    }

    @Test fun registrationConnectsToRepositoryAndShowsVerificationWithoutDemoProfile() {
        val repo = UiAuth()
        launch(repo)
        awaitText("Welcome Back!")
        compose.onNodeWithText("Register").performScrollTo().performClick()
        listOf("Campus Student", "12345", "student@example.test", "local-test-password", "local-test-password")
            .forEachIndexed { index, value -> compose.onAllNodes(hasSetTextAction())[index].performTextInput(value) }
        compose.onNodeWithText("Register").performScrollTo().performClick()
        awaitText("Verify your email")
        assertEquals(1, repo.registerCalls)
        assertNull(repo.user?.claims?.role)
        compose.onNodeWithText("Quick Actions").assertDoesNotExist()
        compose.onNodeWithText("Juan Dela Cruz").assertDoesNotExist()
    }

    @Test fun loginWithVerifiedButMissingRoleShowsPendingAuthorization() {
        val repo = UiAuth()
        repo.verifiedOnLogin = true
        launch(repo)
        awaitText("Welcome Back!")
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("student@example.test")
        compose.onAllNodes(hasSetTextAction())[1].performTextInput("local-test-password")
        compose.onNodeWithText("Log In").performScrollTo().performClick()
        awaitText("Authorization pending")
        compose.onNodeWithText("Admin Dashboard").assertDoesNotExist()
        compose.onNodeWithText("Quick Actions").assertDoesNotExist()
    }

    @Test fun signOutClearsSessionGraphAndBackCannotRestoreOnboarding() {
        val repo = UiAuth()
        repo.user = AuthenticatedUser("local", "student@example.test", "Student", true)
        launch(repo)
        awaitText("Authorization pending")
        compose.onNodeWithText("Sign out").performScrollTo().performClick()
        awaitText("Welcome Back!")
        compose.onNodeWithText("Register").performScrollTo().performClick()
        awaitText("Create Account")
        pressBack()
        compose.onNodeWithText("Welcome Back!").assertIsDisplayed()
        compose.onNodeWithText("Authorization pending").assertDoesNotExist()
        assertNull(repo.user)
    }

    @Test fun sessionCallbackCannotTurnDemoPreviewIntoRealRoleAccess() {
        val repo = UiAuth()
        val auth = launch(repo)
        awaitText("Welcome Back!")
        compose.onNodeWithText("Student", useUnmergedTree = true).performScrollTo().performClick()
        awaitText("Quick Actions")
        compose.runOnIdle {
            repo.emit(AuthenticatedUser("local", "employee@example.test", "Employee", true,
                AuthorizationClaims(UserRole.ADMIN, 1)))
        }
        compose.onNodeWithText("Quick Actions").assertIsDisplayed()
        compose.runOnIdle { auth.returnToAuthentication() }
        awaitText("Profile unavailable")
        compose.onNodeWithText("Admin Dashboard").assertDoesNotExist()
        compose.onNodeWithText("Quick Actions").assertDoesNotExist()
    }

    @Test fun recoveryUsesExistingFormAndGenericConfirmation() {
        val repo = UiAuth()
        launch(repo)
        awaitText("Welcome Back!")
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("student@example.test")
        compose.onNodeWithText("Forgot Password?").performScrollTo().performClick()
        compose.onNodeWithText("Send recovery email").performClick()
        awaitText("If an account can receive recovery email")
        assertEquals(1, repo.recoveryCalls)
        assertNull(repo.user)
    }

    private class UiAuth : AuthRepository {
        private val changesCounter = MutableStateFlow(0)
        override val changes = changesCounter.map { Unit }
        var user: AuthenticatedUser? = null
        var registerCalls = 0
        var recoveryCalls = 0
        var verifiedOnLogin = false
        fun emit(value: AuthenticatedUser?) { user = value; changesCounter.value++ }
        override suspend fun currentUser(forceRefresh: Boolean) = user
        override suspend fun register(fullName: String, email: String, password: String) {
            registerCalls++; emit(AuthenticatedUser("local", email, fullName, false))
        }
        override suspend fun login(email: String, password: String) {
            emit(AuthenticatedUser("local", email, "Student", verifiedOnLogin))
        }
        override suspend fun reload() = Unit
        override suspend fun sendVerification() = Unit
        override suspend fun recoverPassword(email: String) { recoveryCalls++ }
        override fun signOut() { emit(null) }
    }
}
