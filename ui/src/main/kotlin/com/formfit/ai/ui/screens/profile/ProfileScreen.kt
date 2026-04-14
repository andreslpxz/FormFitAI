package com.formfit.ai.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.formfit.ai.core.model.SubscriptionPlan
import com.formfit.ai.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateToPlans: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSignOutDialog by remember { mutableStateOf(false) }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                if (bytes != null) viewModel.uploadAvatar(bytes)
            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "Failed to read image bytes: ${e.message}", e)
            }
        }
    }

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

    if (uiState.isEditingName) {
        AlertDialog(
            onDismissRequest = viewModel::cancelEditName,
            title = { Text("Edit Display Name", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = uiState.editNameBuffer,
                    onValueChange = viewModel::updateNameBuffer,
                    label = { Text("Display name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FormFitTeal,
                        unfocusedBorderColor = SurfaceVariant,
                        focusedLabelColor = FormFitTeal,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = FormFitTeal
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::saveDisplayName) {
                    Text("Save", color = FormFitTeal)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelEditName) {
                    Text("Cancel", color = TextMuted)
                }
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
                            .clickable { avatarPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.avatarUrl != null) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(uiState.avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(2.dp, FormFitGreen, CircleShape),
                                loading = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(FormFitSurface)
                                            .border(2.dp, FormFitGreen, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = FormFitGreen,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                },
                                error = {
                                    AvatarInitial(
                                        initial = uiState.displayName.firstOrNull()?.uppercase() ?: "U"
                                    )
                                }
                            )
                        } else {
                            AvatarInitial(
                                initial = uiState.displayName.firstOrNull()?.uppercase() ?: "U"
                            )
                        }

                        if (uiState.isUploadingAvatar) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = FormFitGreen,
                                    strokeWidth = 2.dp
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(FormFitGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.CameraAlt,
                                    contentDescription = "Change photo",
                                    tint = FormFitNavy,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            uiState.displayName.ifBlank { "Athlete" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = viewModel::startEditingName,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edit name",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

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

@Composable
private fun AvatarInitial(initial: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(FormFitSurface)
            .border(2.dp, FormFitGreen, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = FormFitGreen
        )
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
