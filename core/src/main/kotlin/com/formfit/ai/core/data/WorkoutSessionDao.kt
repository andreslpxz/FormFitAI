package com.formfit.ai.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.formfit.ai.core.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession): Long

    @Query("SELECT * FROM workout_sessions ORDER BY created_at DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE exercise_id = :exerciseId ORDER BY created_at DESC")
    fun getSessionsByExercise(exerciseId: String): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): WorkoutSession?

    @Query("""
        SELECT exercise_id, exercise_name, MAX(rep_count) as rep_count, 
               duration_seconds, avg_form_score, calories_burned, sets_completed,
               form_issues, synced_to_cloud, created_at, id, user_id
        FROM workout_sessions 
        GROUP BY exercise_id
    """)
    fun getPersonalBests(): Flow<List<WorkoutSession>>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE created_at >= :since")
    fun getSessionCountSince(since: Long): Flow<Int>

    @Query("SELECT * FROM workout_sessions WHERE synced_to_cloud = 0")
    suspend fun getUnsyncedSessions(): List<WorkoutSession>

    @Query("UPDATE workout_sessions SET synced_to_cloud = 1 WHERE id = :sessionId")
    suspend fun markAsSynced(sessionId: Long)

    @Query("SELECT * FROM workout_sessions ORDER BY created_at DESC LIMIT 7")
    fun getRecentSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE created_at >= :startMs AND created_at <= :endMs ORDER BY created_at ASC")
    fun getSessionsByDateRange(startMs: Long, endMs: Long): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions ORDER BY created_at DESC LIMIT 30")
    fun getLast30Sessions(): Flow<List<WorkoutSession>>

    @Query("""
        SELECT exercise_id, exercise_name, AVG(avg_form_score) as avg_form_score,
               MAX(rep_count) as rep_count, SUM(calories_burned) as calories_burned,
               MAX(duration_seconds) as duration_seconds, MAX(sets_completed) as sets_completed,
               form_issues, synced_to_cloud, MAX(created_at) as created_at, id, user_id
        FROM workout_sessions
        GROUP BY exercise_id
        ORDER BY avg_form_score DESC
        LIMIT 6
    """)
    fun getFormScoreByExercise(): Flow<List<WorkoutSession>>
}
