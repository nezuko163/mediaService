package com.example.mediaservice.player

import android.content.Context


import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import com.example.mediaservice.Audio
import com.example.mediaservice.utils.Tool
import java.lang.IllegalStateException

class Player(val context: Context) {

    lateinit var mediaPlayer: MediaPlayer
    var isSrcSetted = false

    lateinit var onCompletionListener: MediaPlayer.OnCompletionListener
    lateinit var onErrorListener: MediaPlayer.OnErrorListener

    private fun createMp(resId: Int) {
        createMp(Tool.resIdToUri(context, resId))
    }

    private fun resetAtrributes() {
        Log.i(TAG, "resetAtrributes: $isSrcSetted")
        mediaPlayer.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnCompletionListener(onCompletionListener)
            setOnErrorListener(onErrorListener)
            setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            setVolume(1.0f, 1.0f)
        }
    }

    private fun createMp(uri: Uri) {
        mediaPlayer = MediaPlayer.create(context, uri)
    }

    private fun changeAudio(uri: Uri) {
        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.prepare()
    }

    fun currentTime() = mediaPlayer.currentPosition

    fun play() {
        if (!isSrcSetted) {
            Log.i(TAG, "play: соурс не сдан")
            return
        }
        try {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }

        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun stop() {
        if (isSrcSetted) {
            Log.i(TAG, "stop: 123")
            mediaPlayer.stop()
            mediaPlayer.release()
            isSrcSetted = false
        } else Log.i(TAG, "stop: соурс нот сеттед")
    }

    fun pause() {
        if (!isSrcSetted) {
            Log.i(TAG, "pause: соурс нот сеттед")
            return
        }
        if (mediaPlayer.isPlaying) mediaPlayer.pause()
    }

    fun isPlaying() = mediaPlayer.isPlaying

    fun other(resId: Int) {
        other(Tool.resIdToUri(context, resId))
    }

    fun other(uri: Uri) {
        Log.i(TAG, "other: over started")
        if (::mediaPlayer.isInitialized && isSrcSetted) {
            if (isPlaying()) {
                Log.i(TAG, "other: 123")
                stop()
            }
        }

        createMp(uri)
//        if (isSrcSetted) {
//            Log.i(TAG, "other: srcSetted")
//            changeAudio(uri)
//        } else {
//            Log.i(TAG, "other: srcNotSetted")
//            createMp(uri)
//        }
        resetAtrributes()
        isSrcSetted = true
    }


    fun seekTo(ms: Int) {
        mediaPlayer.seekTo(ms)
    }

    companion object {
        const val TAG = "PLAYER_AUDIO"
    }
}
