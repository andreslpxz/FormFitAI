package com.formfit.ai.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.formfit.ai.core.model.Routine
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine)

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Delete
    suspend fun deleteRoutine(routine: Routine)

    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Query("SELECT * FROM routines WHERE isPreset = 0 ORDER BY createdAt DESC")
    fun getUserRoutines(): Flow<List<Routine>>

    @Query("SELECT * FROM routines WHERE id = :routineId")
    suspend fun getRoutineById(routineId: String): Routine?

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutineById(routineId: String)
}
