package com.testchamber.soloistapp.features.music_player.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.testchamber.soloistapp.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MediaReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_PLAY_PAUSE = "com.testchamber.soloistapp.PLAY_PAUSE"
        const val ACTION_NEXT = "com.testchamber.soloistapp.NEXT"
        const val ACTION_PREVIOUS = "com.testchamber.soloistapp.PREVIOUS"
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pendingResult = goAsync()

        coroutineScope.launch {
            try {
                val app = context.applicationContext as App
                val mediaController = app.appComponent.mediaServiceController()

                when (intent.action) {
                    ACTION_PLAY_PAUSE -> {
                        mediaController.togglePlayPause()
                    }

                    ACTION_NEXT -> {
                        mediaController.next()
                    }

                    ACTION_PREVIOUS -> {
                        mediaController.previous()
                    }
                }
            } catch (e: Exception) {
                Log.e("MediaReceiver", "Error handling action: ${intent.action}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
