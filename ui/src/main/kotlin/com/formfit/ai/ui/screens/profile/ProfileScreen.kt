package com.formfit.ai.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.formfit.ai.core.model.SubscriptionPlan
import com.formfit.ai.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateToPlans: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out", color = Color.White) },
            text = { Text("Are you sure you want to sign out?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    viewModel.signOut()
                    onSignOut()
                }) { Text("Sign Out", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = FormFitSurface
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FormFitNavy),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GradientEnd)
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 28.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(FormFitSurface)
                            .border(2.dp, FormFitGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.displayName.firstOrNull()?.uppercase() ?: "U",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = FormFitGreen
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(uiState.displayName.ifBlank { "Athlete" }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(uiState.email, fontSize = 13.sp, color = TextMuted)

                    Spacer(Modifier.height(12.dp))

                    val plan = uiState.subscriptionPlan
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (plan.isPro()) FormFitGreen.copy(0.15f) else FormFitSurface
                            )
                            .border(1.dp, if (plan.isPro()) FormFitGreen else CardBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (plan.isPro()) "⚡ ${plan.label()}" else "Free Plan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (plan.isPro()) FormFitGreen else TextMuted
                        )
                    }
                }
            }
        }

        if (!uiState.subscriptionPlan.isPro()) {
            item {
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    color = FormFitGreen.copy(0.08f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FormFitGreen.copy(0.3f)),
                    onClick = onNavigateToPlans
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚡", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Upgrade to Pro", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = FormFitGreen)
                            Text("Unlimited workouts · Custom routines · Advanced AI", fontSize = 12.sp, color = TextMuted)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = FormFitGreen)
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            Text("Settings", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(8.dp))
        }

        item {
            ProfileMenuSection(
                items = listOf(
                    ProfileMenuItem(Icons.Filled.VolumeUp, "Sound Effects", uiState.soundEnabled) { viewModel.toggleSound() },
                    ProfileMenuItem(Icons.Filled.Vibration, "Haptics", uiState.hapticsEnabled) { viewModel.toggleHaptics() }
                )
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("Account", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(8.dp))
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                color = FormFitSurface,
                shape = RoundedCornerShape(14.dp),
                onClick = { showSignOutDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Sign Out", fontSize = 15.sp, color = ErrorRed, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

data class ProfileMenuItem(
    val icon: ImageVector,
    val label: String,
    val value: Boolean,
    val onToggle: () -> Unit
)

@Composable
fun ProfileMenuSection(items: List<ProfileMenuItem>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        color = FormFitSurface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(item.icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(item.label, fontSize = 15.sp, color = Color.White, modifier = Modifier.weight(1f))
                    Switch(
                        checked = item.value,
                        onCheckedChange = { item.onToggle() },
                        colors = SwitchDefaults.colors(checkedThumbColor = FormFitNavy, checkedTrackColor = FormFitGreen)
                    )
                }
                if (index < items.size - 1) {
                    Divider(color = CardBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 48.dp))
                }
            }
        }
    }
}
