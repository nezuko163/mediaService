package com.example.mediaservice.service

import android.content.ComponentName
import android.media.MediaPlayer.OnCompletionListener
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import com.example.mediaservice.activity.MainActivity


class MediaBrowserManager(val activity: MainActivity) {

    val TAG = "MEDDIA_MANAGER"
    lateinit var mediaBrowserCompat: MediaBrowserCompat
    lateinit var mediaControllerCompat: MediaControllerCompat

    private val context = activity.applicationContext

    lateinit var controllerCallbackInterface: ControllerCallbackInterface
    lateinit var connectionCallbackInterface: ConnectionCallbackInterface

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()

            mediaControllerCompat = MediaControllerCompat(context, mediaBrowserCompat.sessionToken)
            MediaControllerCompat.setMediaController(activity, mediaControllerCompat)
            Log.i("MAIN_ACTIVITY", "onConnected: mediaController = ${::mediaControllerCompat.isInitialized}")
            mediaControllerCompat.registerCallback(controllerCallback)
            Log.i(
                TAG,
                "onConnected: mediaCOntroller - init = ${::mediaControllerCompat.isInitialized}"
            )
            Toast.makeText(
                activity.applicationContext,
                "mediaCOntroller - init = ${::mediaControllerCompat.isInitialized}",
                Toast.LENGTH_SHORT
            ).show()

            connectionCallbackInterface.onConnected()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()

            Log.i(TAG, "onConnectionFailed: 123")
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()

            Log.i(TAG, "onConnectionSuspended: 321")
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

            controllerCallbackInterface.onMetadataChanged(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)

            controllerCallbackInterface.onPlaybackStateChanged(state)
        }

    }

    fun onStart() {
        if (!::mediaBrowserCompat.isInitialized) {
            Log.i(TAG, "onStart: 123")
            mediaBrowserCompat = MediaBrowserCompat(
                context,
                ComponentName(context, MediaPlaybackService::class.java),  // 创建callback
                connectionCallback,
                null
            )
            mediaBrowserCompat.connect()
        } else {
            Log.i(TAG, "onStart: not connected")
        }
    }

    fun onStop() {
        if (::mediaControllerCompat.isInitialized) {
            Log.i(TAG, "onStop: manager on stop")
            mediaController()?.stop()
            mediaControllerCompat.unregisterCallback(controllerCallback)
        }
    }

    fun mediaController(): MediaControllerCompat.TransportControls? {
        if (::mediaControllerCompat.isInitialized) {
            return mediaControllerCompat.transportControls
        }
        return null
    }
}