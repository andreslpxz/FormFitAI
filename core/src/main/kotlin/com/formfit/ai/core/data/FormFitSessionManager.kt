package com.formfit.ai.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.gotrue.SessionManager
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormFitSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SessionManager {

    companion object {
        private const val PREFS_FILE = "formfit_session"
        private const val KEY_SESSION = "supabase_session"
    }

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun loadSession(): UserSession? {
        val sessionJson = prefs.getString(KEY_SESSION, null) ?: return null
        return try {
            json.decodeFromString<UserSession>(sessionJson)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveSession(session: UserSession) {
        try {
            val sessionJson = json.encodeToString(session)
            prefs.edit().putString(KEY_SESSION, sessionJson).apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    override suspend fun deleteSession() {
        prefs.edit().remove(KEY_SESSION).apply()
    }
}
