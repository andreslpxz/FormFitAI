package com.formfit.ai.core.data

import android.util.Log
import com.formfit.ai.core.model.WorkoutSession
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

private const val TAG = "WorkoutRepository"
private const val FREE_WEEKLY_WORKOUT_LIMIT = 3

class WeeklyLimitReachedException :
    RuntimeException("Weekly workout limit reached. Upgrade to Pro for unlimited workouts.")

class WorkoutRepository(
    private val workoutSessionDao: WorkoutSessionDao,
    private val supabaseClient: SupabaseClient,
    private val subscriptionRepository: SubscriptionRepository
) {

    suspend fun saveSession(session: WorkoutSession): Long = withContext(Dispatchers.IO) {
        val authenticatedUserId = supabaseClient.auth.currentUserOrNull()?.id ?: ""
        val sessionWithUser = if (session.userId.isBlank() && authenticatedUserId.isNotBlank()) {
            session.copy(userId = authenticatedUserId)
        } else {
            session
        }

        if (!subscriptionRepository.currentPlan.isPro()) {
            val weekStart = getStartOfWeekMs()
            val count = workoutSessionDao.getWeeklySessionCount(weekStart)
            if (count >= FREE_WEEKLY_WORKOUT_LIMIT) {
                Log.w(TAG, "Free user hit weekly workout limit ($count/$FREE_WEEKLY_WORKOUT_LIMIT)")
                throw WeeklyLimitReachedException()
            }
        }

        val id = workoutSessionDao.insertSession(sessionWithUser)
        trySync(sessionWithUser.copy(id = id))
        id
    }

    private fun getStartOfWeekMs(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
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
