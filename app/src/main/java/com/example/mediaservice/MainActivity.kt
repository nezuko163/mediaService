package com.example.mediaservice

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat.QueueItem
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.ThumbRating
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediaservice.adapter.AudioAdapter
import com.example.mediaservice.databinding.ActivityMainBinding
import com.example.mediaservice.service.ConnectionCallbackInterface
import com.example.mediaservice.service.ControllerCallbackInterface
import com.example.mediaservice.service.MediaBrowserManager
import com.example.mediaservice.utils.AllAudios
import com.example.mediaservice.utils.NotificationHelper
import com.example.mediaservice.utils.PermissionUtil
import com.example.mediaservice.utils.Tool
import com.squareup.picasso.Picasso
import kotlin.math.log

private const val PERMISSION_STORAGE = 8000


class MainActivity : AppCompatActivity() {

    val TAG = "MAIN_ACTIVITY"

    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaBrowserManager: MediaBrowserManager
    private val adapter = AudioAdapter()
    private val picasso = Picasso.get()

    private lateinit var list: ArrayList<Audio>
    private var isPlaying = false

    private val listener = object : ControllerCallbackInterface {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.i(TAG, "onMetadataChanged: yeah")
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            if (state?.state == PlaybackStateCompat.STATE_PLAYING) {
                binding.btn.setImageResource(R.drawable.pause)
                Log.i(TAG, "onPlaybackStateChanged: playing")
                isPlaying = true
            }
            if (state?.state == PlaybackStateCompat.STATE_PAUSED) {
                binding.btn.setImageResource(R.drawable.play)
                Log.i(TAG, "onPlaybackStateChanged: pause")
                isPlaying = false
            }
        }
    }

    private val connnectionCallback = object : ConnectionCallbackInterface {
        override fun onConnected() {
            for (i in 0..<list.size) {
                mediaBrowserManager.mediaControllerCompat.addQueueItem(
                    Tool.audioToMediaDescriptionCompat(
                        list[i],
                        i.toLong()
                    )
                )
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        NotificationHelper.createChannelForMediaPlayerNotification(this)

        requirePermission()
    }

    private fun init() {
        initUi()
        initMediaBrowserManager()

        initList()
        initRcView()
    }

    private fun initUi() {
        binding.btn.setOnClickListener {
            Log.i(TAG, "initUi: 123")
            mediaBrowserManager.mediaController()?.let {
                if (isPlaying) it.pause()
                else it.play()
            }
        }
    }

    private fun initMediaBrowserManager() {
        mediaBrowserManager = MediaBrowserManager(this)
        mediaBrowserManager.controllerCallbackInterface = listener
        mediaBrowserManager.connectionCallbackInterface = connnectionCallback
    }

    private fun initList() {
        list = AllAudios.getAudios(applicationContext)
        Log.i(TAG, "initList: 123")
    }

    private fun initRcView() {
        adapter.setAudioList(list)
        adapter.onItemClick = { audio: Audio ->
            if (audio.audio_uri != null) {
                bindCurrentPlayingTrack(audio)
                binding.curTrack.visibility = View.VISIBLE
                mediaBrowserManager.mediaController()?.skipToQueueItem(adapter.pos.toLong())
                mediaBrowserManager.mediaController()?.play()
            }
        }

        binding.rcView.layoutManager = LinearLayoutManager(this)
        binding.rcView.adapter = adapter
    }

    private fun bindCurrentPlayingTrack(audio: Audio) {
        binding.apply {
            if (audio.art_uri != null) {
                picasso
                    .load(audio.art_uri)
                    .into(img)
            }
            audioName.text = audio.name
            authorName.text = audio.artist
        }
    }

    private fun requirePermission() {
        if (PermissionUtil.hasExternalStoragePermission(this)) {
//            Toast.makeText(this, "aeee", Toast.LENGTH_LONG).show()
            init()
        } else {
//            Toast.makeText(this, "neeeeeeeeeeeeeet", Toast.LENGTH_LONG).show()
            PermissionUtil.requestPermissions(this, PERMISSION_STORAGE)
        }

        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 16302)
        }
    }

    override fun onStart() {
        super.onStart()

        if (::mediaBrowserManager.isInitialized) {
            mediaBrowserManager.onStart()
            Log.i(TAG, "onStart: get started")
        }
    }

    override fun onStop() {
        super.onStop()

        if (::mediaBrowserManager.isInitialized) {
            mediaBrowserManager.onStop()
        }
    }
}