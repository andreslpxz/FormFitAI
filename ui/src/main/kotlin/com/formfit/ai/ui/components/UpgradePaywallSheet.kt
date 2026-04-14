package com.formfit.ai.ui.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formfit.ai.ui.theme.*

private const val TAG = "UpgradePaywallSheet"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradePaywallSheet(
    title: String = "Pro Feature",
    description: String = "Upgrade to FormFit Pro to unlock this feature.",
    checkoutUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    fun openCheckout() {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl)))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open checkout URL: ${e.message}", e)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FormFitSurface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(FormFitGreen.copy(alpha = 0.3f), FormFitTeal.copy(alpha = 0.3f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("⚡", fontSize = 36.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                title,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                description,
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            val proFeatures = listOf(
                "Unlimited workouts every week",
                "Custom routines",
                "Advanced analytics & charts",
                "AI form coaching priority"
            )
            proFeatures.forEach { feature ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = FormFitGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(feature, color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = ::openCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FormFitGreen,
                    contentColor = FormFitNavy
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Upgrade to Pro — $9.99/month",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(10.dp))

            TextButton(onClick = onDismiss) {
                Text("Maybe later", color = TextMuted, fontSize = 13.sp)
            }

            Text(
                "Cancel anytime · Secure checkout",
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
