package com.sss.mysimilarteenieping

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

private const val TAG = "MySimilarTeeniepingApp"

@HiltAndroidApp
class MySimilarTeeniepingApp : Application() {
    override fun onCreate() {
        Log.d(TAG, "onCreate: triggered")
        super.onCreate()
    }
} 