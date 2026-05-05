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

    @Query("SELECT * FROM workout_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE exerciseId = :exerciseId ORDER BY createdAt DESC")
    fun getSessionsByExercise(exerciseId: String): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): WorkoutSession?

    @Query("""
        SELECT exerciseId, exerciseName, MAX(repCount) as repCount, 
               durationSeconds, avgFormScore, caloriesBurned, setsCompleted,
               formIssues, syncedToCloud, createdAt, id, userId
        FROM workout_sessions 
        GROUP BY exerciseId
    """)
    fun getPersonalBests(): Flow<List<WorkoutSession>>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE createdAt >= :since")
    fun getSessionCountSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE createdAt >= :weekStart")
    suspend fun getWeeklySessionCount(weekStart: Long): Int

    @Query("SELECT * FROM workout_sessions WHERE syncedToCloud = 0")
    suspend fun getUnsyncedSessions(): List<WorkoutSession>

    @Query("UPDATE workout_sessions SET syncedToCloud = 1 WHERE id = :sessionId")
    suspend fun markAsSynced(sessionId: Long)

    @Query("SELECT * FROM workout_sessions ORDER BY createdAt DESC LIMIT 7")
    fun getRecentSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE createdAt >= :startMs AND createdAt <= :endMs ORDER BY createdAt ASC")
    fun getSessionsByDateRange(startMs: Long, endMs: Long): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions ORDER BY createdAt DESC LIMIT 30")
    fun getLast30Sessions(): Flow<List<WorkoutSession>>

    @Query("""
        SELECT exerciseId, exerciseName, AVG(avgFormScore) as avgFormScore,
               MAX(repCount) as repCount, SUM(caloriesBurned) as caloriesBurned,
               MAX(durationSeconds) as durationSeconds, MAX(setsCompleted) as setsCompleted,
               formIssues, syncedToCloud, MAX(createdAt) as createdAt, id, userId
        FROM workout_sessions
        GROUP BY exerciseId
        ORDER BY avgFormScore DESC
        LIMIT 6
    """)
    fun getFormScoreByExercise(): Flow<List<WorkoutSession>>
}
