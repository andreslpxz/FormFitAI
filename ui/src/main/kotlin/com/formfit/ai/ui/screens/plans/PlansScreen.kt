package com.formfit.ai.ui.screens.plans

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.SubscriptionRepository
import com.formfit.ai.core.model.SubscriptionPlan
import com.formfit.ai.ui.BuildConfig
import com.formfit.ai.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PlansScreen"

data class PlansUiState(
    val currentPlan: SubscriptionPlan = SubscriptionPlan.FREE,
    val selectedBilling: BillingPeriod = BillingPeriod.MONTHLY,
    val isLoading: Boolean = false,
    val pendingUrl: String? = null,
    val errorMessage: String? = null
)

enum class BillingPeriod { MONTHLY, YEARLY }

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlansUiState())
    val uiState: StateFlow<PlansUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            subscriptionRepository.refreshSubscription()
            subscriptionRepository.subscriptionPlan.collect { plan ->
                _uiState.update { it.copy(currentPlan = plan) }
            }
        }
    }

    fun selectBilling(period: BillingPeriod) {
        _uiState.update { it.copy(selectedBilling = period) }
    }

    fun requestCheckout() {
        val priceId = if (_uiState.value.selectedBilling == BillingPeriod.MONTHLY) {
            BuildConfig.STRIPE_PRICE_MONTHLY
        } else {
            BuildConfig.STRIPE_PRICE_YEARLY
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val url = subscriptionRepository.createCheckoutSession(priceId)
                _uiState.update { it.copy(isLoading = false, pendingUrl = url) }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Auth error starting checkout: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Please sign in to continue.") }
            } catch (e: Exception) {
                Log.e(TAG, "Checkout session error: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not start checkout. Please try again.") }
            }
        }
    }

    fun requestBillingPortal() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val url = subscriptionRepository.createPortalSession()
                _uiState.update { it.copy(isLoading = false, pendingUrl = url) }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Auth error opening portal: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Please sign in to manage billing.") }
            } catch (e: Exception) {
                Log.e(TAG, "Portal session error: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not open billing portal. Please try again.") }
            }
        }
    }

    fun consumePendingUrl() {
        _uiState.update { it.copy(pendingUrl = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

@Composable
fun PlansScreen(
    onBack: () -> Unit,
    viewModel: PlansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.pendingUrl) {
        val url = uiState.pendingUrl ?: return@LaunchedEffect
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open URL: ${e.message}", e)
        }
        viewModel.consumePendingUrl()
    }

    uiState.errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Checkout Unavailable", color = Color.White) },
            text = { Text(msg, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text("OK", color = FormFitTeal)
                }
            },
            containerColor = FormFitSurface
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FormFitNavy)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        "Plans & Pricing",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    FormFitGreen.copy(alpha = 0.15f),
                                    FormFitNavy
                                )
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚡", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Unlock Full Potential",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "AI-powered form coaching, unlimited workouts,\nand detailed analytics — all in FormFit Pro.",
                            fontSize = 14.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            if (uiState.currentPlan.isPro()) {
                item {
                    ProActiveBanner(
                        plan = uiState.currentPlan,
                        onManageBilling = viewModel::requestBillingPortal,
                        isLoading = uiState.isLoading
                    )
                }
            } else {
                item {
                    BillingToggle(
                        selected = uiState.selectedBilling,
                        onSelect = viewModel::selectBilling,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    PlanComparisonCard(
                        selectedBilling = uiState.selectedBilling,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            item {
                FeatureComparisonTable(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Cancel anytime · Billed via Stripe",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Secure checkout · SSL encrypted",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (!uiState.currentPlan.isPro()) {
            Button(
                onClick = viewModel::requestCheckout,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FormFitGreen,
                    contentColor = FormFitNavy
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = FormFitNavy,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (uiState.selectedBilling == BillingPeriod.MONTHLY)
                            "Start Pro — $9.99/month"
                        else
                            "Start Pro — $59.99/year  (save 50%)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun BillingToggle(
    selected: BillingPeriod,
    onSelect: (BillingPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = FormFitSurface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            BillingToggleOption(
                label = "Monthly",
                isSelected = selected == BillingPeriod.MONTHLY,
                onClick = { onSelect(BillingPeriod.MONTHLY) },
                modifier = Modifier.weight(1f)
            )
            BillingToggleOption(
                label = "Yearly  🏷️ Save 50%",
                isSelected = selected == BillingPeriod.YEARLY,
                onClick = { onSelect(BillingPeriod.YEARLY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BillingToggleOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) FormFitGreen else Color.Transparent,
        animationSpec = tween(200),
        label = "billingBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) FormFitNavy else TextMuted,
        animationSpec = tween(200),
        label = "billingText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun PlanComparisonCard(
    selectedBilling: BillingPeriod,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PlanTierCard(
            name = "Free",
            price = "$0",
            period = "forever",
            color = TextMuted,
            isHighlighted = false,
            modifier = Modifier.weight(1f)
        )
        PlanTierCard(
            name = "Pro",
            price = if (selectedBilling == BillingPeriod.MONTHLY) "$9.99" else "$4.99",
            period = if (selectedBilling == BillingPeriod.MONTHLY) "per month" else "per month\nbilled annually",
            color = FormFitGreen,
            isHighlighted = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PlanTierCard(
    name: String,
    price: String,
    period: String,
    color: Color,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.then(
            if (isHighlighted) Modifier.border(1.5.dp, color, RoundedCornerShape(16.dp))
            else Modifier
        ),
        color = FormFitSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isHighlighted) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(FormFitGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text("POPULAR", fontSize = 9.sp, color = FormFitGreen, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(6.dp))
            } else {
                Spacer(Modifier.height(21.dp))
            }
            Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(price, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(period, fontSize = 11.sp, color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun FeatureComparisonTable(modifier: Modifier = Modifier) {
    val features = listOf(
        Triple("Pose detection (60 FPS)", true, true),
        Triple("6 exercise types", true, true),
        Triple("Preset routines", true, true),
        Triple("Rep counting & timer", true, true),
        Triple("Workouts per week", false, true),
        Triple("Custom routines", false, true),
        Triple("Advanced analytics & charts", false, true),
        Triple("Form score insights", false, true),
        Triple("Progress history (unlimited)", false, true),
        Triple("Priority AI coaching", false, true),
        Triple("Body stats tracking", false, true)
    )

    Surface(modifier = modifier, color = FormFitSurface, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Feature", color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(
                    "Free",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(52.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    "Pro",
                    color = FormFitGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(52.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            HorizontalDivider(
                color = SurfaceVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            features.forEachIndexed { index, (label, free, pro) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Box(Modifier.width(52.dp), contentAlignment = Alignment.Center) {
                        if (free) {
                            Icon(Icons.Rounded.Check, null, tint = FormFitGreen, modifier = Modifier.size(16.dp))
                        } else {
                            Text("3/wk", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                    Box(Modifier.width(52.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Check, null, tint = FormFitGreen, modifier = Modifier.size(16.dp))
                    }
                }
                if (index < features.size - 1) {
                    HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun ProActiveBanner(
    plan: SubscriptionPlan,
    onManageBilling: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        color = FormFitGreen.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FormFitGreen.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✅", fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "You're on ${plan.label()}",
                        color = FormFitGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("All Pro features unlocked", color = TextSecondary, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onManageBilling,
                enabled = !isLoading,
                border = androidx.compose.foundation.BorderStroke(1.dp, FormFitGreen.copy(0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FormFitGreen)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = FormFitGreen,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Manage Billing", fontSize = 14.sp)
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}
