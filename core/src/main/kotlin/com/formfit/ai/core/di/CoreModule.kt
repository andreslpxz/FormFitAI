package com.formfit.ai.core.di

import android.content.Context
import androidx.room.Room
import com.formfit.ai.core.data.AuthRepository
import com.formfit.ai.core.data.FormFitSessionManager
import com.formfit.ai.core.data.PreferencesManager
import com.formfit.ai.core.data.ProfileRepository
import com.formfit.ai.core.data.SupabaseClientFactory
import com.formfit.ai.core.data.WorkoutDatabase
import com.formfit.ai.core.data.WorkoutSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager = PreferencesManager(context)

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): FormFitSessionManager = FormFitSessionManager(context)

    @Provides
    @Singleton
    fun provideSupabaseClient(
        sessionManager: FormFitSessionManager
    ): SupabaseClient = SupabaseClientFactory.create(sessionManager)

    @Provides
    @Singleton
    fun provideWorkoutDatabase(
        @ApplicationContext context: Context
    ): WorkoutDatabase =
        Room.databaseBuilder(
            context,
            WorkoutDatabase::class.java,
            "formfit_database"
        ).build()

    @Provides
    @Singleton
    fun provideWorkoutSessionDao(database: WorkoutDatabase): WorkoutSessionDao =
        database.workoutSessionDao()

    @Provides
    @Singleton
    fun provideProfileRepository(
        supabaseClient: SupabaseClient
    ): ProfileRepository = ProfileRepository(supabaseClient)

    @Provides
    @Singleton
    fun provideAuthRepository(
        supabaseClient: SupabaseClient
    ): AuthRepository = AuthRepository(supabaseClient)
}
