package com.testchamber.soloistapp.features.music_player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.testchamber.soloistapp.App
import com.testchamber.soloistapp.R
import jakarta.inject.Inject

class MediaService : Service() {
    @Inject
    lateinit var mediaController: MediaServiceController

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "MediaPlayerChannel"
    }

    override fun onCreate() {
        super.onCreate()
        (applicationContext as App).appComponent.inject(this)

        createNotificationChannel()

        val initialNotification = createInitialNotification()
        startForeground(NOTIFICATION_ID, initialNotification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Media Player",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Media player controls"
                    setShowBadge(false)
                }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createInitialNotification(): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE,
            )

        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note_24dp)
            .setContentTitle("Music Player")
            .setContentText("Loading...")
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        mediaController.updateNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        mediaController.release()
        super.onDestroy()
    }
}
