package com.formfit.ai.core.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClientFactory {
    const val SUPABASE_URL = "https://tnjahnkoeziadabetlvx.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRuamFobmtvZXppYWRhYmV0bHZ4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTQ3MDAwMDAsImV4cCI6MjAzMDI3NjAwMH0.placeholder_key_replace_with_actual"

    fun create(): SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(GoTrue)
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}
