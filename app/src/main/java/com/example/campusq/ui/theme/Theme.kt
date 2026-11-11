package com.example.campusq.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Light-mode reference-matching color scheme (Part 5)
private val CampusQLightColorScheme = lightColorScheme(
    primary = CampusQGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = CampusQGreenLight,
    onPrimaryContainer = CampusQGreenDark,
    secondary = CampusQGreenDark,
    onSecondary = Color.White,
    secondaryContainer = CampusQGreenLight,
    onSecondaryContainer = CampusQGreenDark,
    tertiary = CampusQAdminPurple,
    onTertiary = Color.White,
    tertiaryContainer = CampusQAdminPurpleLight,
    onTertiaryContainer = CampusQAdminPurpleDark,
    background = CampusQBackground,
    onBackground = CampusQTextPrimary,
    surface = CampusQSurface,
    onSurface = CampusQTextPrimary,
    surfaceVariant = CampusQSurfaceVariant,
    onSurfaceVariant = CampusQTextSecondary,
    outline = CampusQOutline,
    error = CampusQError,
    onError = Color.White,
    errorContainer = CampusQErrorBg,
    onErrorContainer = CampusQError
)

// Spacing & Dimensions Tokens (Part 7)
object CampusQDimens {
    val SpaceExtraSmall = 4.dp
    val SpaceSmall = 8.dp
    val SpaceMedium = 12.dp
    val SpaceStandard = 16.dp
    val SpaceLarge = 24.dp
    val SpaceExtraLarge = 32.dp

    val ScreenHorizontalPadding = 16.dp
    val CardRadius = 16.dp
    val FeatureCardRadius = 20.dp
    val ButtonRadius = 12.dp
    val TextFieldRadius = 12.dp
    val ButtonHeight = 50.dp
    val MinTouchTarget = 48.dp
}

@Composable
fun CampusQTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CampusQLightColorScheme,
        typography = Typography,
        content = content
    )
}
