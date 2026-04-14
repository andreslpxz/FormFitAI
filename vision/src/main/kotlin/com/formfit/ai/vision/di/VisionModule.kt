package com.formfit.ai.vision.di

import android.content.Context
import com.formfit.ai.vision.PoseAnalysisManager
import com.formfit.ai.vision.PoseLandmarkerHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VisionModule {

    @Provides
    @Singleton
    fun providePoseLandmarkerHelper(
        @ApplicationContext context: Context
    ): PoseLandmarkerHelper = PoseLandmarkerHelper(context)

    @Provides
    @Singleton
    fun providePoseAnalysisManager(): PoseAnalysisManager = PoseAnalysisManager()
}
