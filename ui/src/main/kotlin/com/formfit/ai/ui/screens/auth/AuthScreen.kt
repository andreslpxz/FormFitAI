package com.formfit.ai.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.formfit.ai.ui.components.FormFitPrimaryButton
import com.formfit.ai.ui.theme.*

@Composable
fun AuthScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var magicLinkEmail by remember { mutableStateOf("") }
    var showMagicLinkInput by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(FormFitNavy, GradientEnd))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(FormFitGreen, Color(0xFF00ACC1)))),
                contentAlignment = Alignment.Center
            ) {
                Text("FF", fontSize = 32.sp, fontWeight = FontWeight.Black, color = FormFitNavy)
            }

            Spacer(Modifier.height(24.dp))

            Text("FormFit AI", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text(
                "Your AI-powered personal trainer",
                fontSize = 15.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))

            GoogleSignInButton(
                onClick = { viewModel.signInWithGoogle(context) },
                isLoading = uiState.isGoogleLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = CardBorder)
                Text("  or  ", color = TextMuted, fontSize = 13.sp)
                Divider(modifier = Modifier.weight(1f), color = CardBorder)
            }

            Spacer(Modifier.height(12.dp))

            FormFitPrimaryButton(
                text = "Continue with Email",
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            if (showMagicLinkInput) {
                OutlinedTextField(
                    value = magicLinkEmail,
                    onValueChange = { magicLinkEmail = it },
                    label = { Text("Your email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FormFitBlue,
                        focusedLabelColor = FormFitBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = {
                    if (!showMagicLinkInput) {
                        showMagicLinkInput = true
                    } else {
                        viewModel.sendMagicLink(magicLinkEmail)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FormFitBlue),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, FormFitBlue.copy(0.4f))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = FormFitBlue, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (showMagicLinkInput) "Send Magic Link" else "✉️  Sign in with Magic Link",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = TextMuted, fontSize = 14.sp)
                Text(
                    "Create one",
                    color = FormFitGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(Modifier.height(32.dp))

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = ErrorRed, fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
            }

            if (uiState.magicLinkSent) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = FormFitGreen.copy(0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "✅ Magic link sent! Check your email.",
                        modifier = Modifier.padding(12.dp),
                        color = FormFitGreen,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            Text(
                "By continuing, you agree to our Terms & Privacy Policy",
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = FormFitNavy,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("G", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                Spacer(Modifier.width(10.dp))
                Text("Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F1F1F))
            }
        }
    }
}
