package com.pliniopalmer.tomaya

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TomaYaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val canal = NotificationChannel(
            CANAL_TOMAS,
            "Tomas",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Avisos a la hora de cada toma"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    companion object {
        const val CANAL_TOMAS = "tomas"
    }
}
