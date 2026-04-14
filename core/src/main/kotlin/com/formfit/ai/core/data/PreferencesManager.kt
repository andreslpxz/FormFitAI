package com.formfit.ai.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "formfit_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_GENDER = stringPreferencesKey("user_gender")
        val KEY_FITNESS_GOAL = stringPreferencesKey("fitness_goal")
        val KEY_WORKOUT_FREQUENCY = stringPreferencesKey("workout_frequency")
        val KEY_EQUIPMENT_LEVEL = stringPreferencesKey("equipment_level")
        val KEY_REFERRAL_SOURCE = stringPreferencesKey("referral_source")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val KEY_LAST_EXERCISE_ID = stringPreferencesKey("last_exercise_id")
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_ONBOARDING_COMPLETE] ?: false }

    val soundEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_SOUND_ENABLED] ?: true }

    val hapticsEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_HAPTICS_ENABLED] ?: true }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETE] = complete
        }
    }

    suspend fun saveOnboardingData(
        name: String,
        gender: String,
        goal: String,
        frequency: String,
        equipment: String,
        referral: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_GENDER] = gender
            prefs[KEY_FITNESS_GOAL] = goal
            prefs[KEY_WORKOUT_FREQUENCY] = frequency
            prefs[KEY_EQUIPMENT_LEVEL] = equipment
            prefs[KEY_REFERRAL_SOURCE] = referral
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_HAPTICS_ENABLED] = enabled }
    }

    fun getOnboardingData(): Flow<OnboardingData> = context.dataStore.data.map { prefs ->
        OnboardingData(
            name = prefs[KEY_USER_NAME] ?: "",
            gender = prefs[KEY_USER_GENDER] ?: "",
            goal = prefs[KEY_FITNESS_GOAL] ?: "",
            frequency = prefs[KEY_WORKOUT_FREQUENCY] ?: "",
            equipment = prefs[KEY_EQUIPMENT_LEVEL] ?: "",
            referral = prefs[KEY_REFERRAL_SOURCE] ?: ""
        )
    }
}

data class OnboardingData(
    val name: String,
    val gender: String,
    val goal: String,
    val frequency: String,
    val equipment: String,
    val referral: String
)
