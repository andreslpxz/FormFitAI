package com.formfit.ai.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.formfit.ai.core.model.BodyWeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BodyWeightEntry): Long

    @Query("SELECT * FROM body_weights ORDER BY date DESC")
    fun getAllEntries(): Flow<List<BodyWeightEntry>>

    @Query("SELECT * FROM body_weights ORDER BY date DESC LIMIT 1")
    fun getLatestEntry(): Flow<BodyWeightEntry?>

    @Query("SELECT * FROM body_weights WHERE date >= :since ORDER BY date ASC")
    fun getEntriesSince(since: Long): Flow<List<BodyWeightEntry>>
}
