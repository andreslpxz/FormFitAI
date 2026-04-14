package com.formfit.ai.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.formfit.ai.core.model.Routine
import com.formfit.ai.core.model.WorkoutSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Database(
    entities = [WorkoutSession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutSessionDao(): WorkoutSessionDao
}

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: String): List<String> =
        if (value.isEmpty()) emptyList()
        else json.decodeFromString(value)

    @TypeConverter
    fun toStringList(list: List<String>): String = json.encodeToString(list)
}
