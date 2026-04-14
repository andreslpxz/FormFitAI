package com.formfit.ai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FormFitApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
