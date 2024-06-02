package com.example.mediaservice.service

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat

interface ControllerCallbackInterface {
    fun onMetadataChanged(metadata: MediaMetadataCompat?)
    fun onPlaybackStateChanged(state: PlaybackStateCompat?)
}