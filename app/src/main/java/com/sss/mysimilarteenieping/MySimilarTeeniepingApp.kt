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
        // 필요한 경우 앱 초기화 코드 추가
        // 예: Timber 초기화, Firebase Crashlytics 등
    }
} 