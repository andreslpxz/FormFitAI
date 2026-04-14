package com.formfit.ai.core.data

import android.util.Log
import com.formfit.ai.core.model.SubscriptionPlan
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SubscriptionRepository"

@Singleton
class SubscriptionRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val _subscriptionPlan = MutableStateFlow(SubscriptionPlan.FREE)
    val subscriptionPlan: Flow<SubscriptionPlan> = _subscriptionPlan.asStateFlow()

    val currentPlan: SubscriptionPlan get() = _subscriptionPlan.value

    suspend fun refreshSubscription() {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: run {
            Log.w(TAG, "Cannot refresh subscription: no authenticated user")
            return
        }
        try {
            val row = supabaseClient.postgrest["profiles"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<Map<String, String>>()

            val planStr = row?.get("subscription_plan") ?: "free"
            val plan = when (planStr) {
                "pro_monthly" -> SubscriptionPlan.PRO_MONTHLY
                "pro_yearly" -> SubscriptionPlan.PRO_YEARLY
                "pro" -> SubscriptionPlan.PRO_MONTHLY
                else -> SubscriptionPlan.FREE
            }
            _subscriptionPlan.value = plan
            Log.d(TAG, "Subscription refreshed: $plan")
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Supabase client state error refreshing subscription: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh subscription: ${e.javaClass.simpleName} — ${e.message}", e)
        }
    }

    fun buildCheckoutUrl(priceId: String): String {
        val supabaseUrl = "https://tnjahnkoeziadabetlvx.supabase.co"
        return "$supabaseUrl/functions/v1/create-checkout-session?priceId=$priceId"
    }

    fun buildPortalUrl(): String {
        val supabaseUrl = "https://tnjahnkoeziadabetlvx.supabase.co"
        return "$supabaseUrl/functions/v1/customer-portal"
    }
}
