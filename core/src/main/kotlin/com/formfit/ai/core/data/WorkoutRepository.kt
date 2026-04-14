package com.formfit.ai.core.data

import android.util.Log
import com.formfit.ai.core.model.WorkoutSession
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

private const val TAG = "WorkoutRepository"

class WorkoutRepository(
    private val workoutSessionDao: WorkoutSessionDao,
    private val supabaseClient: SupabaseClient
) {

    suspend fun saveSession(session: WorkoutSession): Long = withContext(Dispatchers.IO) {
        val authenticatedUserId = supabaseClient.auth.currentUserOrNull()?.id ?: ""
        val sessionWithUser = if (session.userId.isBlank() && authenticatedUserId.isNotBlank()) {
            session.copy(userId = authenticatedUserId)
        } else {
            session
        }
        val id = workoutSessionDao.insertSession(sessionWithUser)
        trySync(sessionWithUser.copy(id = id))
        id
    }

    suspend fun getSessionById(id: Long): WorkoutSession? = withContext(Dispatchers.IO) {
        workoutSessionDao.getSessionById(id)
    }

    fun getRecentSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getRecentSessions()

    fun getAllSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getAllSessions()

    suspend fun syncPendingSessions() = withContext(Dispatchers.IO) {
        val unsynced = workoutSessionDao.getUnsyncedSessions()
        Log.d(TAG, "Syncing ${unsynced.size} unsynced sessions")
        unsynced.forEach { session ->
            trySync(session)
        }
    }

    private suspend fun trySync(session: WorkoutSession) {
        if (session.userId.isBlank()) {
            Log.w(TAG, "Skipping cloud sync for session ${session.id}: no authenticated user")
            return
        }
        try {
            supabaseClient.from("workout_sessions").insert(session)
            workoutSessionDao.markAsSynced(session.id)
            Log.d(TAG, "Synced session ${session.id} to Supabase")
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Supabase client state error syncing session ${session.id}: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync session ${session.id} to Supabase: ${e.javaClass.simpleName} — ${e.message}", e)
        }
    }
}
