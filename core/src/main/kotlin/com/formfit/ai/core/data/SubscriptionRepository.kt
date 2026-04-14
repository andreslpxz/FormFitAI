package com.formfit.ai.core.data

import android.util.Log
import com.formfit.ai.core.model.SubscriptionPlan
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class ProfilePlanRow(
    @SerialName("subscription_plan") val subscriptionPlan: String? = null
)

private const val TAG = "SubscriptionRepository"
private const val SUPABASE_URL = "https://tnjahnkoeziadabetlvx.supabase.co"
private const val SUPABASE_ANON_KEY =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRuamFobmtvZXppYWRhYmV0bHZ4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzYxODAzMTcsImV4cCI6MjA5MTc1NjMxN30.FyNhQzf_jurZiFGWxdyDyojMroDpyO36BPRnQ378ops"

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
                .select(columns = Columns.list("subscription_plan")) {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<ProfilePlanRow>()

            val planStr = row?.subscriptionPlan ?: "free"
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
        } catch (e: RuntimeException) {
            Log.e(TAG, "Failed to refresh subscription: ${e.javaClass.simpleName} — ${e.message}", e)
        }
    }

    suspend fun createCheckoutSession(priceId: String): String {
        val accessToken = supabaseClient.auth.currentSessionOrNull()?.accessToken
            ?: throw IllegalStateException("Not authenticated — cannot start checkout")

        return withContext(Dispatchers.IO) {
            val endpoint = "$SUPABASE_URL/functions/v1/create-checkout-session?priceId=${java.net.URLEncoder.encode(priceId, "UTF-8")}"
            val conn = URL(endpoint).openConnection() as HttpURLConnection
            conn.connectTimeout = 20_000
            conn.readTimeout = 20_000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $accessToken")
            conn.setRequestProperty("apikey", SUPABASE_ANON_KEY)
            try {
                val code = conn.responseCode
                val body = if (code == 200) {
                    conn.inputStream.bufferedReader().readText()
                } else {
                    val err = conn.errorStream?.bufferedReader()?.readText() ?: ""
                    throw IOException("Checkout session request failed ($code): $err")
                }
                JSONObject(body).getString("url")
            } finally {
                conn.disconnect()
            }
        }
    }

    suspend fun createPortalSession(): String {
        val accessToken = supabaseClient.auth.currentSessionOrNull()?.accessToken
            ?: throw IllegalStateException("Not authenticated — cannot open billing portal")

        return withContext(Dispatchers.IO) {
            val endpoint = "$SUPABASE_URL/functions/v1/customer-portal"
            val conn = URL(endpoint).openConnection() as HttpURLConnection
            conn.connectTimeout = 20_000
            conn.readTimeout = 20_000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $accessToken")
            conn.setRequestProperty("apikey", SUPABASE_ANON_KEY)
            try {
                val code = conn.responseCode
                val body = if (code == 200) {
                    conn.inputStream.bufferedReader().readText()
                } else {
                    val err = conn.errorStream?.bufferedReader()?.readText() ?: ""
                    throw IOException("Portal session request failed ($code): $err")
                }
                JSONObject(body).getString("url")
            } finally {
                conn.disconnect()
            }
        }
    }
}
