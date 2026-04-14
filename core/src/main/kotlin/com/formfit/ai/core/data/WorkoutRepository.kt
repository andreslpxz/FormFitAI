package com.formfit.ai.core.data

import com.formfit.ai.core.model.WorkoutSession
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WorkoutRepository(
    private val workoutSessionDao: WorkoutSessionDao,
    private val supabaseClient: SupabaseClient
) {

    suspend fun saveSession(session: WorkoutSession): Long = withContext(Dispatchers.IO) {
        val id = workoutSessionDao.insertSession(session)
        trySync(session.copy(id = id))
        id
    }

    suspend fun getSessionById(id: Long): WorkoutSession? = withContext(Dispatchers.IO) {
        workoutSessionDao.getSessionById(id)
    }

    fun getRecentSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getRecentSessions()

    fun getAllSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getAllSessions()

    suspend fun syncPendingSessions() = withContext(Dispatchers.IO) {
        val unsynced = workoutSessionDao.getUnsyncedSessions()
        unsynced.forEach { session ->
            trySync(session)
        }
    }

    private suspend fun trySync(session: WorkoutSession) {
        try {
            supabaseClient.from("workout_sessions").insert(session)
            workoutSessionDao.markAsSynced(session.id)
        } catch (_: Exception) {
        }
    }
}
