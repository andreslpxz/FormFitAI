package com.formfit.ai.core.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.android.Android

object SupabaseClientFactory {
    const val SUPABASE_URL = "https://tnjahnkoeziadabetlvx.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
        ".eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRuamFobmtvZXppYWRhYmV0bHZ4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzYxODAzMTcsImV4cCI6MjA5MTc1NjMxN30" +
        ".FyNhQzf_jurZiFGWxdyDyojMroDpyO36BPRnQ378ops"

    fun create(sessionManager: FormFitSessionManager? = null): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            httpEngine = Android.create()
            install(Auth) {
                if (sessionManager != null) {
                    this.sessionManager = sessionManager
                }
                alwaysAutoRefresh = false
                autoLoadFromStorage = true
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
}
