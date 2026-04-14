package com.formfit.ai.vision.di

import android.content.Context
import com.formfit.ai.vision.CameraManager
import com.formfit.ai.vision.PoseAnalysisManager
import com.formfit.ai.vision.PoseLandmarkerHelper
import com.formfit.ai.vision.RepCounterEngine
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
    fun providePoseAnalysisManager(): PoseAnalysisManager = PoseAnalysisManager()

    @Provides
    @Singleton
    fun providePoseLandmarkerHelper(
        @ApplicationContext context: Context
    ): PoseLandmarkerHelper = PoseLandmarkerHelper(context)

    @Provides
    @Singleton
    fun provideCameraManager(
        @ApplicationContext context: Context,
        poseLandmarkerHelper: PoseLandmarkerHelper
    ): CameraManager = CameraManager(context, poseLandmarkerHelper)

    @Provides
    @Singleton
    fun provideRepCounterEngine(
        poseAnalysisManager: PoseAnalysisManager
    ): RepCounterEngine = RepCounterEngine(poseAnalysisManager)

    @Provides
    @Singleton
    fun providePoseLandmarkSmoother(): com.formfit.ai.vision.PoseLandmarkSmoother =
        com.formfit.ai.vision.PoseLandmarkSmoother()
}
