package com.formfit.ai.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.formfit.ai.ui.components.FormFitPrimaryButton
import com.formfit.ai.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            Text("🔑", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text("Forgot Password?", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(
                "Enter your email and we'll send you a reset link",
                fontSize = 15.sp,
                color = TextMuted,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(40.dp))

            FormFitTextField(
                value = uiState.email,
                onValueChange = viewModel::setEmail,
                label = "Email address",
                keyboardType = KeyboardType.Email
            )

            if (uiState.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(uiState.errorMessage!!, color = ErrorRed, fontSize = 13.sp)
            }

            if (uiState.emailSent) {
                Spacer(Modifier.height(16.dp))
                Surface(color = FormFitGreen.copy(0.1f), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) {
                    Text(
                        "✅ Reset link sent! Check your inbox.",
                        modifier = Modifier.padding(12.dp),
                        color = FormFitGreen,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            FormFitPrimaryButton(
                text = "Send Reset Link",
                onClick = viewModel::sendResetEmail,
                enabled = uiState.email.isNotBlank() && !uiState.emailSent,
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
