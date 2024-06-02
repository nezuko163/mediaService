package com.example.mediaservice.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.support.v4.media.session.MediaSessionCompat

class NoisyReceiver(val mediaSessionCompat: MediaSessionCompat) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            mediaSessionCompat.controller.transportControls.pause()
        }
    }
}