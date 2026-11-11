package com.example.campusq.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusq.R
import com.example.campusq.ui.theme.CampusQDimens
import com.example.campusq.ui.theme.CampusQError
import com.example.campusq.ui.theme.CampusQErrorBg
import com.example.campusq.ui.theme.CampusQGreenDark
import com.example.campusq.ui.theme.CampusQGreenPrimary
import com.example.campusq.ui.theme.CampusQOutline
import com.example.campusq.ui.theme.CampusQSurface
import com.example.campusq.ui.theme.CampusQTextPrimary
import com.example.campusq.ui.theme.CampusQTextSecondary

@Composable
fun CampusQFullLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 76.dp,
    showTagline: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_campusq_icon),
            contentDescription = "CampusQ Logo",
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Campus",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = CampusQGreenDark,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Q",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = CampusQGreenPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
        if (showTagline) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Queue smarter. Wait less.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CampusQTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun CampusQPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    containerColor: Color = CampusQGreenDark
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CampusQDimens.ButtonHeight),
        shape = RoundedCornerShape(CampusQDimens.ButtonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.8f)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun CampusQSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CampusQDimens.ButtonHeight),
        shape = RoundedCornerShape(CampusQDimens.ButtonRadius),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (enabled) CampusQGreenPrimary else CampusQOutline
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CampusQSurface,
            contentColor = CampusQGreenDark,
            disabledContentColor = CampusQTextSecondary.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun CampusQDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CampusQDimens.ButtonHeight),
        shape = RoundedCornerShape(CampusQDimens.ButtonRadius),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (enabled) CampusQError else CampusQOutline
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CampusQErrorBg.copy(alpha = 0.35f),
            contentColor = CampusQError,
            disabledContentColor = CampusQTextSecondary.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun CampusQTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = CampusQGreenDark
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
            color = color
        )
    }
}

@Composable
fun CampusQTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    errorMessage: String? = null,
    supportingText: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder, color = CampusQTextSecondary.copy(alpha = 0.7f)) }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = CampusQGreenPrimary
                    )
                }
            } else null,
            isError = errorMessage != null,
            supportingText = {
                when {
                    errorMessage != null -> Text(text = errorMessage, color = CampusQError)
                    supportingText != null -> Text(text = supportingText, color = CampusQTextSecondary)
                }
            },
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(CampusQDimens.TextFieldRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CampusQGreenPrimary,
                unfocusedBorderColor = CampusQOutline,
                focusedLabelColor = CampusQGreenDark,
                unfocusedLabelColor = CampusQTextSecondary,
                focusedContainerColor = CampusQSurface,
                unfocusedContainerColor = CampusQSurface,
                errorContainerColor = CampusQSurface,
                focusedTextColor = CampusQTextPrimary,
                unfocusedTextColor = CampusQTextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CampusQPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = CampusQGreenPrimary
                    )
                }
            } else null,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = CampusQTextSecondary
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = errorMessage != null,
            supportingText = if (errorMessage != null) {
                { Text(text = errorMessage, color = CampusQError) }
            } else null,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(CampusQDimens.TextFieldRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CampusQGreenPrimary,
                unfocusedBorderColor = CampusQOutline,
                focusedLabelColor = CampusQGreenDark,
                unfocusedLabelColor = CampusQTextSecondary,
                focusedContainerColor = CampusQSurface,
                unfocusedContainerColor = CampusQSurface,
                errorContainerColor = CampusQSurface,
                focusedTextColor = CampusQTextPrimary,
                unfocusedTextColor = CampusQTextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CampusQSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search services..."
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = CampusQTextSecondary) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = CampusQGreenPrimary
            )
        },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = CampusQTextSecondary
                    )
                }
            }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CampusQGreenPrimary,
            unfocusedBorderColor = CampusQOutline,
            focusedContainerColor = CampusQSurface,
            unfocusedContainerColor = CampusQSurface,
            focusedTextColor = CampusQTextPrimary,
            unfocusedTextColor = CampusQTextPrimary
        ),
        modifier = modifier.fillMaxWidth()
    )
}
