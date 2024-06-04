package com.example.mediaservice.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediaservice.Audio
import com.example.mediaservice.R
import com.example.mediaservice.adapter.AudioAdapter
import com.example.mediaservice.adapter.OptionsMenuClickListener
import com.example.mediaservice.databinding.ActivityMainBinding
import com.example.mediaservice.fragment.BottomSheetEventListener
import com.example.mediaservice.fragment.ControlPlayingTrackFragment
import com.example.mediaservice.fragment.CurrentPlayingTrackFragment
import com.example.mediaservice.fragment.MyBottomSheet
import com.example.mediaservice.fragment.MyFragmentFactory
import com.example.mediaservice.service.TransportControlInterface
import com.example.mediaservice.service.ConnectionCallbackInterface
import com.example.mediaservice.service.ControllerCallbackInterface
import com.example.mediaservice.service.MediaBrowserManager
import com.example.mediaservice.utils.AllAudios
import com.example.mediaservice.utils.NotificationHelper
import com.example.mediaservice.utils.PermissionUtil
import com.example.mediaservice.utils.Tool
import com.example.mediaservice.viewmodels.AudioViewModel
import com.example.mediaservice.viewmodels.PlayBackViewModel
import com.example.mediaservice.viewmodels.PlayQueueViewModel
import com.squareup.picasso.Picasso

private const val PERMISSION_STORAGE = 8000


class MainActivity : AppCompatActivity() {

    val TAG = "MAIN_ACTIVITY"

    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaBrowserManager: MediaBrowserManager
    private lateinit var btmSheet: MyBottomSheet

    private val picasso = Picasso.get()
    private lateinit var adapter: AudioAdapter

    private lateinit var list: ArrayList<Audio>
    private val metadataViewModel: AudioViewModel by viewModels()
    private val playBackViewModel: PlayBackViewModel by viewModels()
    private val playQueueViewModel: PlayQueueViewModel by viewModels()
    private var lastBtmSheetState = 0

    private val listener = object : ControllerCallbackInterface {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            if (metadata == null) return
            metadataViewModel.selectMetadata(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            if (state == null) return
            playBackViewModel.setPlaying(state)
        }
    }

    private val menuItemListener = object : OptionsMenuClickListener {
        override fun addToQueue(position: Int) {
        }

        override fun addToPlaylist(position: Int) {
        }

        override fun changeCredits(position: Int) {
        }

    }

    private val transportController = object : TransportControlInterface {
        override fun skipToNext() {
            mediaBrowserManager.mediaController()?.skipToNext()
        }

        override fun skipToPrevious() {
            mediaBrowserManager.mediaController()?.skipToPrevious()
        }

        override fun skipTo(id: Int) {
            mediaBrowserManager.mediaController()?.skipToQueueItem(id.toLong())
        }

        override fun seekTo(ms: Long) {
            mediaBrowserManager.mediaController()?.seekTo(ms)
        }

        override fun play() {
            mediaBrowserManager.mediaController()?.play()
        }

        override fun pause() {
            mediaBrowserManager.mediaController()?.pause()
        }


        override fun stop() {
            mediaBrowserManager.mediaController()?.stop()
        }
    }

    private val connnectionCallback = object : ConnectionCallbackInterface {
        override fun onConnected() {
            for (i in 0..<list.size) {
                val item = Tool.audioToMediaDescriptionCompat(
                    list[i],
                    i.toLong()
                )
                mediaBrowserManager.mediaControllerCompat.addQueueItem(
                    item
                )
            }
            mediaBrowserManager.mediaController()
                ?.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val fragment = supportFragmentManager.findFragmentById(R.id.container)
            if (fragment == null) {
                onBackPressedDispatcher.onBackPressed()
            } else {
                if (fragment is ControlPlayingTrackFragment) {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace(R.id.container, CurrentPlayingTrackFragment::class.java, null)
                    }
                } else onBackPressedDispatcher.onBackPressed()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    private fun initUi() {
        Log.i(TAG, "initUi: ${binding.container.parent::class.java}")
//        btmSheet = MyBottomSheet(binding.container, btmListener)
        supportFragmentManager.fragmentFactory = MyFragmentFactory(transportController)

        metadataViewModel.selectedMetadata.observe(this) {
            val metadata = it ?: return@observe
            updateRecyclerAfterTrackChanged(metadata)
        }

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.container, CurrentPlayingTrackFragment::class.java, null)
        }
    }


    private fun initMediaBrowserManager() {
        mediaBrowserManager = MediaBrowserManager(this)
        mediaBrowserManager.controllerCallbackInterface = listener
        mediaBrowserManager.connectionCallbackInterface = connnectionCallback
    }

    private fun initList() {
        list = AllAudios.getAudios(applicationContext)
        playQueueViewModel.set(list)
    }

    private fun initRcView() {
        adapter = AudioAdapter(this, menuItemListener)
        adapter.setAudioList(list)
        adapter.onItemClick = { audio: Audio ->
            if (audio.audio_uri != null) {
                mediaBrowserManager.mediaController()?.skipToQueueItem(adapter.pos.toLong())
                mediaBrowserManager.mediaController()?.play()
            }
        }

        binding.rcView.layoutManager = LinearLayoutManager(this)
        binding.rcView.adapter = adapter
    }

    private fun updateRecyclerAfterTrackChanged(metadata: MediaMetadataCompat) {
        val id =
            metadata.bundle?.getLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS) ?: return

        val view = binding.rcView.layoutManager?.findViewByPosition(id.toInt()) ?: return
        val lastTrack = binding.rcView.getChildViewHolder(view) as AudioAdapter.AudioViewHolder
        adapter.trackChanged(lastTrack, id)
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
            != PackageManager.PERMISSION_GRANTED
        ) {
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
        Log.i(TAG, "onStop: 123123123")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaBrowserManager.isInitialized) {
            Log.i(TAG, "onStop: 321321")
            mediaBrowserManager.onStop()
        }
    }
}