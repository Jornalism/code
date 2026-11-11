package com.example.campusq.ui.auth

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusq.R
import com.example.campusq.ui.components.CampusQCard
import com.example.campusq.ui.components.CampusQDemoLabel
import com.example.campusq.ui.components.CampusQFullLogo
import com.example.campusq.ui.components.CampusQPasswordField
import com.example.campusq.ui.components.CampusQPrimaryButton
import com.example.campusq.ui.components.CampusQSecondaryButton
import com.example.campusq.ui.components.CampusQTextField
import com.example.campusq.ui.theme.CampusQAdminPurple
import com.example.campusq.ui.theme.CampusQAdminPurpleLight
import com.example.campusq.ui.theme.CampusQBackground
import com.example.campusq.ui.theme.CampusQDimens
import com.example.campusq.ui.theme.CampusQGreenDark
import com.example.campusq.ui.theme.CampusQGreenLight
import com.example.campusq.ui.theme.CampusQGreenPrimary
import com.example.campusq.ui.theme.CampusQInfo
import com.example.campusq.ui.theme.CampusQInfoBg
import com.example.campusq.ui.theme.CampusQOutline
import com.example.campusq.ui.theme.CampusQSurface
import com.example.campusq.ui.theme.CampusQTextPrimary
import com.example.campusq.ui.theme.CampusQTextSecondary
import kotlinx.coroutines.delay

/**
 * SCREEN 01 — SPLASH SCREEN
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDemoHub: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1400L)
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CampusQSurface)
            .clickable { onNavigateToLogin() }
    ) {
        // Welcome campus background artwork
        Image(
            painter = painterResource(id = R.drawable.img_campus_welcome_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.88f)
        )

        // Subtle top-to-bottom readability scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color.White.copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.24f))

            CampusQFullLogo(
                iconSize = 112.dp,
                showTagline = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = CampusQGreenLight.copy(alpha = 0.9f)
            ) {
                Text(
                    text = "Smart Queue & Appointment Management",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CampusQGreenDark,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.55f))

            Surface(
                shape = RoundedCornerShape(50),
                color = CampusQGreenDark.copy(alpha = 0.95f),
                modifier = Modifier.clickable(onClick = onNavigateToDemoHub)
            ) {
                Text(
                    text = "Tap to continue • UI Preview Mode",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * SCREEN 02 — LOGIN SCREEN
 */
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToDemoHub: () -> Unit,
    onOpenStudentDemo: () -> Unit,
    onOpenStaffDemo: () -> Unit,
    onOpenAdminDemo: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var showAuthNotConnectedDialog by rememberSaveable { mutableStateOf(false) }
    var showForgotPasswordDialog by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CampusQSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                CampusQDemoLabel()
            }

            Spacer(modifier = Modifier.height(8.dp))

            CampusQFullLogo(
                iconSize = 72.dp,
                showTagline = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Log in to your account",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            CampusQTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = "Email Address",
                placeholder = "juan.delacruz@campusq.edu.ph",
                leadingIcon = Icons.Default.Email,
                errorMessage = emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampusQPasswordField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                errorMessage = passwordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showForgotPasswordDialog = true }) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = CampusQGreenDark,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CampusQPrimaryButton(
                text = "Log In",
                onClick = {
                    var valid = true
                    if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                        emailError = "Enter a valid school email address"
                        valid = false
                    }
                    if (password.length < 6) {
                        passwordError = "Password must be at least 6 characters"
                        valid = false
                    }
                    if (valid) {
                        showAuthNotConnectedDialog = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.labelLarge.copy(color = CampusQGreenDark),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onNavigateToRegister)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = CampusQOutline)
            Spacer(modifier = Modifier.height(16.dp))

            // Clearly separated Development UI Preview Card (Part 10 requirement)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CampusQBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = CampusQGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "UI Preview / Demo Mode (Frontend Only)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = CampusQGreenDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Authentication backend is not connected in this UI phase. Use Role Preview to inspect all 23 screens:",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoleQuickChip(
                            title = "Student",
                            color = CampusQGreenDark,
                            onClick = onOpenStudentDemo,
                            modifier = Modifier.weight(1f)
                        )
                        RoleQuickChip(
                            title = "Staff",
                            color = CampusQGreenDark,
                            onClick = onOpenStaffDemo,
                            modifier = Modifier.weight(1f)
                        )
                        RoleQuickChip(
                            title = "Admin",
                            color = CampusQAdminPurple,
                            onClick = onOpenAdminDemo,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onNavigateToDemoHub) {
                        Text(
                            text = "Open Full 23-Screen UI Preview Hub →",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = CampusQGreenDark,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }

    if (showAuthNotConnectedDialog) {
        AlertDialog(
            onDismissRequest = { showAuthNotConnectedDialog = false },
            title = { Text("Authentication Not Connected", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "This build is the CampusQ UI/UX prototype. Firebase Authentication is not yet connected.\n\nTo inspect and test the application interfaces, please select a role in UI Preview Mode.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAuthNotConnectedDialog = false
                        onNavigateToDemoHub()
                    }
                ) {
                    Text("Open UI Preview", color = CampusQGreenDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthNotConnectedDialog = false }) {
                    Text("Close", color = CampusQTextSecondary)
                }
            },
            containerColor = CampusQSurface
        )
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Password Recovery (UI Preview)", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Password reset emails require backend authentication integration. In production, a recovery link will be sent to your institutional email address.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary)
                )
            },
            confirmButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Understood", color = CampusQGreenDark, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CampusQSurface
        )
    }
}

@Composable
private fun RoleQuickChip(
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (color == CampusQGreenPrimary) CampusQGreenDark else color
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White,
                    fontSize = 13.sp
                )
            )
        }
    }
}

/**
 * SCREEN 03 — CREATE ACCOUNT / REGISTRATION
 */
@Composable
fun RegistrationScreen(
    onNavigateBackToLogin: () -> Unit,
    onNavigateToDemoHub: () -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var studentId by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var fullNameError by rememberSaveable { mutableStateOf<String?>(null) }
    var studentIdError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmPasswordError by rememberSaveable { mutableStateOf<String?>(null) }

    var showDemoNoticeDialog by rememberSaveable { mutableStateOf(false) }

    val isFormFilled = fullName.isNotBlank() &&
        studentId.isNotBlank() &&
        email.isNotBlank() &&
        password.isNotBlank() &&
        confirmPassword.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CampusQSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBackToLogin) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Login",
                    tint = CampusQTextPrimary
                )
            }
            CampusQDemoLabel()
        }

        Image(
            painter = painterResource(id = R.drawable.img_campusq_icon),
            contentDescription = "CampusQ Icon",
            modifier = Modifier.size(60.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Join CampusQ Today",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        CampusQTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                fullNameError = null
            },
            label = "Full Name",
            placeholder = "Juan Dela Cruz",
            leadingIcon = Icons.Default.Person,
            errorMessage = fullNameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        CampusQTextField(
            value = studentId,
            onValueChange = {
                studentId = it
                studentIdError = null
            },
            label = "Student ID",
            placeholder = "2023-10482",
            leadingIcon = Icons.Default.Badge,
            errorMessage = studentIdError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        CampusQTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = "Email Address",
            placeholder = "juan.delacruz@campusq.edu.ph",
            leadingIcon = Icons.Default.Email,
            errorMessage = emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        CampusQPasswordField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            errorMessage = passwordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )
        )

        CampusQPasswordField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = null
            },
            label = "Confirm Password",
            leadingIcon = Icons.Default.Lock,
            errorMessage = confirmPasswordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        CampusQPrimaryButton(
            text = "Register",
            enabled = isFormFilled,
            onClick = {
                var valid = true
                if (fullName.trim().length < 3) {
                    fullNameError = "Please enter your full name"
                    valid = false
                }
                if (studentId.trim().length < 5) {
                    studentIdError = "Please enter a valid Student ID"
                    valid = false
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                    emailError = "Please enter a valid email address"
                    valid = false
                }
                if (password.length < 6) {
                    passwordError = "Password must be at least 6 characters"
                    valid = false
                }
                if (confirmPassword != password) {
                    confirmPasswordError = "Passwords do not match"
                    valid = false
                }
                if (valid) {
                    showDemoNoticeDialog = true
                }
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Log In",
                style = MaterialTheme.typography.labelLarge.copy(color = CampusQGreenDark),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onNavigateBackToLogin)
                    .padding(4.dp)
            )
        }
    }

    if (showDemoNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showDemoNoticeDialog = false },
            title = { Text("Registration UI Preview", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Form validation passed for $fullName ($studentId).\n\nBecause this phase is strictly UI/UX, no backend account was created. You can now explore the Student interface in UI Preview mode.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = CampusQTextPrimary)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDemoNoticeDialog = false
                        onNavigateToDemoHub()
                    }
                ) {
                    Text("Open UI Preview", color = CampusQGreenDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDemoNoticeDialog = false }) {
                    Text("Stay Here", color = CampusQTextSecondary)
                }
            },
            containerColor = CampusQSurface
        )
    }
}

/**
 * UI PREVIEW / DEMO ROLE SELECTOR HUB (Part 10 requirement)
 */
@Composable
fun DemoRolePreviewHubScreen(
    onOpenStudent: () -> Unit,
    onOpenStaff: () -> Unit,
    onOpenAdmin: () -> Unit,
    onNavigateToSplash: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CampusQBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CampusQCard(
            cornerRadius = 20.dp,
            containerColor = CampusQSurface,
            contentPadding = 20.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.img_campusq_icon),
                        contentDescription = "CampusQ Logo",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CampusQ UI Preview",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Queue smarter. Wait less.",
                            style = MaterialTheme.typography.bodySmall.copy(color = CampusQGreenDark)
                        )
                    }
                }
                CampusQDemoLabel()
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CampusQInfoBg
            ) {
                Text(
                    text = "Development Preview Only: Select any role below to inspect and test all 23 CampusQ screens with shared in-memory demonstration state.",
                    style = MaterialTheme.typography.bodySmall.copy(color = CampusQInfo),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Role Interfaces",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        RolePreviewCard(
            title = "Student Interface",
            subtitle = "Screens 04–09 • Home, Registrar Services, Queue Status, Book Appointment, History, Profile",
            badgeText = "6 Screens",
            icon = Icons.Default.School,
            accentColor = CampusQGreenPrimary,
            accentBg = CampusQGreenLight,
            onClick = onOpenStudent
        )

        Spacer(modifier = Modifier.height(10.dp))

        RolePreviewCard(
            title = "Registrar Staff Interface",
            subtitle = "Screens 10–16 • Dashboard, Queue Management, Ticket Details, Service in Progress, Appointments, Service Status, Profile",
            badgeText = "7 Screens",
            icon = Icons.Default.SupportAgent,
            accentColor = CampusQGreenDark,
            accentBg = CampusQGreenLight,
            onClick = onOpenStaff
        )

        Spacer(modifier = Modifier.height(10.dp))

        RolePreviewCard(
            title = "Administrator Interface",
            subtitle = "Screens 17–23 • Admin Dashboard, Manage Services, Staff Management, Queue Monitoring, Appointments, Reports, System Settings",
            badgeText = "7 Screens",
            icon = Icons.Default.AdminPanelSettings,
            accentColor = CampusQAdminPurple,
            accentBg = CampusQAdminPurpleLight,
            onClick = onOpenAdmin
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Entry & Authentication Screens (Screens 01–03)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CampusQSecondaryButton(
                text = "01 Splash",
                onClick = onNavigateToSplash,
                modifier = Modifier.weight(1f)
            )
            CampusQSecondaryButton(
                text = "02 Login",
                onClick = onNavigateToLogin,
                modifier = Modifier.weight(1f)
            )
            CampusQSecondaryButton(
                text = "03 Register",
                onClick = onNavigateToRegister,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RolePreviewCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: ImageVector,
    accentColor: Color,
    accentBg: Color,
    onClick: () -> Unit
) {
    CampusQCard(
        onClick = onClick,
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = accentBg,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CampusQTextPrimary
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accentBg
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (accentColor == CampusQGreenPrimary) CampusQGreenDark else accentColor,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = CampusQTextSecondary)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open $title",
                tint = accentColor
            )
        }
    }
}
