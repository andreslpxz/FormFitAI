package com.formfit.ai.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.formfit.ai.ui.components.FormFitPrimaryButton
import com.formfit.ai.ui.theme.*

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onRegisterSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(FormFitNavy, GradientEnd)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Spacer(Modifier.height(32.dp))
            Text("Create Account 🚀", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text("Start your fitness journey today", fontSize = 15.sp, color = TextMuted)

            Spacer(Modifier.height(40.dp))

            FormFitTextField(
                value = uiState.displayName,
                onValueChange = viewModel::setDisplayName,
                label = "Full name"
            )
            Spacer(Modifier.height(12.dp))

            FormFitTextField(
                value = uiState.email,
                onValueChange = viewModel::setEmail,
                label = "Email address",
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))

            FormFitTextField(
                value = uiState.password,
                onValueChange = viewModel::setPassword,
                label = "Password (min 8 characters)",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = TextMuted
                        )
                    }
                }
            )

            if (uiState.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(uiState.errorMessage!!, color = ErrorRed, fontSize = 13.sp)
            }

            Spacer(Modifier.weight(1f))

            FormFitPrimaryButton(
                text = "Create Account",
                onClick = viewModel::register,
                enabled = uiState.displayName.isNotBlank() && uiState.email.isNotBlank() && uiState.password.length >= 8,
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
