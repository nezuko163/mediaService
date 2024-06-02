package com.example.mediaservice.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.QueueItem
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.media.AudioManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media3.exoplayer.ExoPlayer
import com.example.mediaservice.MainActivity
import com.example.mediaservice.R
import com.example.mediaservice.player.Player
import com.example.mediaservice.utils.Pashalko
import com.example.mediaservice.utils.NotificationHelper
import com.example.mediaservice.utils.Tool


class MediaPlaybackService : MediaBrowserServiceCompat() {

    val TAG = "SERVICE_MEDIA_AUDIO"
    val CHANNEL_ID = "1488"

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioFocusRequest: AudioFocusRequest

    //    private lateinit var noisyReceiver: NoisyReceiver
    private lateinit var player: Player


    private val handler = Handler(Looper.getMainLooper())
    private var currentQueueItemId = -1
    private val list = ArrayList<QueueItem>()

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                mediaSession.controller.transportControls.pause()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    val onCompletionListener = MediaPlayer.OnCompletionListener {
        val repeatMode = mediaSession.controller.repeatMode
        when (repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_ONE -> {}
            PlaybackStateCompat.REPEAT_MODE_ALL -> callback.onSkipToNext()
            else -> callback.onStop()
        }

        callback.onPrepare()
        callback.onPlay()
    }

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaSession.controller.transportControls.pause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player.mediaPlayer.setVolume(
                0.3f,
                0.3f
            )

            AudioManagerCompat.AUDIOFOCUS_GAIN -> player.mediaPlayer.setVolume(1.0f, 1.0f)
        }
    }

    val callback = object : MediaSessionCompat.Callback() {

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            val keyEvent: KeyEvent? =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    // Pre-SDK 33
                    @Suppress("DEPRECATION")
                    mediaButtonEvent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                } else {
                    // SDK 33 and up
                    mediaButtonEvent?.getParcelableExtra(
                        Intent.EXTRA_KEY_EVENT,
                        KeyEvent::class.java
                    )
                }
            Log.i(TAG, "onMediaButtonEvent: $keyEvent")
            keyEvent?.let { event ->
                when (event.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        if (player.isPlaying()) onPause()
                        else onPlay()
                    }

                    KeyEvent.KEYCODE_MEDIA_PLAY -> onPlay()
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> onPause()
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> onSkipToPrevious()
                    KeyEvent.KEYCODE_MEDIA_NEXT -> onSkipToNext()
                }
            }


            return super.onMediaButtonEvent(mediaButtonEvent)
        }

        override fun onPlay() {
            super.onPlay()

            Log.i(TAG, "onPlay: ")
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setOnAudioFocusChangeListener(afChangeListener)
                setAudioAttributes(AudioAttributes.Builder().run {
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                build()
            }
            val result = am.requestAudioFocus(audioFocusRequest)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                startService(Intent(applicationContext, MediaBrowserService::class.java))
                mediaSession.isActive = true
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                player.play()
                registerReceiver(
                    noisyReceiver,
                    IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                )
//                val thread = Thread(playerPosition)
//                Thread(Player) start ()
                setMediaMetadata()
                refreshNotification()

            }
        }

        override fun onStop() {
            super.onStop()

            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.abandonAudioFocusRequest(audioFocusRequest)
            unregisterReceiver(noisyReceiver)
            stopSelf()
            mediaSession.isActive = false
            player.stop()
            stopForeground(NOTIFIC_CHANNEL_ID)
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
            Log.i(TAG, "onCommand: 123")
        }

        override fun onPause() {
            super.onPause()

            Log.i(TAG, "onPause: ")

            player.pause()
            unregisterReceiver(noisyReceiver)
            refreshNotification()
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
            stopForeground(NOTIFIC_CHANNEL_ID)
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)

            player.seekTo(pos.toInt())
        }

        override fun onSkipToQueueItem(id: Long) {
            super.onSkipToQueueItem(id)
            Log.i(TAG, "onSkipToQueueItem: $id")

            if (id >= list.size || id < 0) return

            currentQueueItemId = id.toInt()

            onPrepare()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            Log.i(TAG, "onSkipToPrevious: $currentQueueItemId")
            onSkipToQueueItem((currentQueueItemId - 1).toLong())
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            Log.i(TAG, "onSkipToNext: $currentQueueItemId")
            if (currentQueueItemId == list.size - 1) {
                if (mediaSession.controller.repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                    onSkipToQueueItem(0)
                } else return
            }

            onSkipToQueueItem((currentQueueItemId + 1).toLong())
        }

        override fun onPrepare() {
            super.onPrepare()

            Log.i(TAG, "onPrepare: 321")

            val track = list[currentQueueItemId]
            val state = mediaSession.controller.playbackState.state

            Log.i(TAG, "onPrepare: ${track.description.mediaId.toString()}")

            val uri = track.description.mediaUri ?: return
            Log.i(TAG, "onPrepare: prepare")
            player.other(uri)

            setMediaMetadata()
            setMediaPlaybackState(PlaybackStateCompat.STATE_NONE)
            refreshNotification()

            if (state == PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM ||
                state == PlaybackStateCompat.STATE_PLAYING
            ) {
                onPlay()
            }
        }

        override fun onAddQueueItem(description: MediaDescriptionCompat?, index: Int) {
            super.onAddQueueItem(description, index)

            val item = QueueItem(description, index.toLong())
            list.add(index, item)

            mediaSession.setQueue(list)

        }

        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            super.onAddQueueItem(description)
            onAddQueueItem(description, list.size)
        }
    }


    private fun setMediaPlaybackState(state: Int, bundle: Bundle? = null) {
        val playbackPosition = player.currentTime().toLong() ?: 0L
        val playbackSpeed = player.mediaPlayer.playbackParams.speed ?: 0f
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setState(state, playbackPosition, playbackSpeed)
            .setActiveQueueItemId(currentQueueItemId.toLong())
        playbackStateBuilder.setExtras(bundle)
        playbackStateBuilder.setState(state, playbackPosition, playbackSpeed)
        mediaSession.setPlaybackState(playbackStateBuilder.build())
    }

    private fun setMediaMetadata() {
        mediaSession.setMetadata(
            Tool.metadataBuilder(
                list[currentQueueItemId],
                applicationContext
            )?.build()
        )
    }

    private fun refreshNotification() {
        val builder = NotificationHelper.notificationBuilder(
            applicationContext,
            mediaSession,
            player.isPlaying(),
            getString(R.string.NOTIFICATION_CHANNEL_ID)
        )
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Log.i(TAG, "refreshNotification: ${nm.areNotificationsEnabled()}")


        startForeground(14881, builder.build())
        nm.notify(14881, builder.build())
    }

    //
//    private fun refreshNotification() {
//        val isPlaying = mediaPlayer?.isPlaying ?: false
//        val playPauseIntent = if (isPlaying) {
//            Intent(applicationContext, MediaPlaybackService::class.java).setAction("pause")
//        } else Intent(applicationContext, MediaPlaybackService::class.java).setAction("play")
//        val nextIntent =
//            Intent(applicationContext, MediaPlaybackService::class.java).setAction("next")
//        val prevIntent =
//            Intent(applicationContext, MediaPlaybackService::class.java).setAction("previous")
//
//        val intent = packageManager
//            .getLaunchIntentForPackage(packageName)
//            ?.setPackage(null)
//            ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
//        val activityIntent =
//            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//
//        val builder = NotificationCompat.Builder(
//            applicationContext,
//            getString(R.string.NOTIFICATION_CHANNEL_ID)
//        ).apply {
//            val mediaMetadata = mediaSession.controller.metadata
//
//            // Previous button
//            addAction(
//                NotificationCompat.Action(
//                    R.drawable.baseline_fast_rewind_24, getString(R.string.play),
//                    PendingIntent.getService(
//                        applicationContext,
//                        0,
//                        prevIntent,
//                        PendingIntent.FLAG_IMMUTABLE
//                    )
//                )
//            )
//
//            // Play/pause button
//            val playOrPause = if (isPlaying) R.drawable.pause
//            else R.drawable.play
//            addAction(
//                NotificationCompat.Action(
//                    playOrPause, getString(R.string.play),
//                    PendingIntent.getService(
//                        applicationContext,
//                        0,
//                        playPauseIntent,
//                        PendingIntent.FLAG_IMMUTABLE
//                    )
//                )
//            )
//
//            // Next button
//            addAction(
//                NotificationCompat.Action(
//                    R.drawable.ic_next, getString(R.string.play),
//                    PendingIntent.getService(
//                        applicationContext,
//                        0,
//                        nextIntent,
//                        PendingIntent.FLAG_IMMUTABLE
//                    )
//                )
//            )
//
//            setStyle(
//                androidx.media.app.NotificationCompat.MediaStyle()
//                    .setShowActionsInCompactView(0, 1, 2)
//                    .setMediaSession(mediaSessionCompat.sessionToken)
//            )
//
//            val smallIcon = if (isPlaying) R.drawable.play
//            else R.drawable.pause
//            setSmallIcon(smallIcon)
//
//            setContentIntent(activityIntent)
//
//            // Add the metadata for the currently playing track
//            setContentTitle(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
//            setContentText(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
//            setLargeIcon(mediaMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
//
//            // Make the transport controls visible on the lockscreen
//            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            priority = NotificationCompat.PRIORITY_DEFAULT
//        }
//        // Display the notification and place the service in the foreground
//        startForeground(1, builder.build())
//    }

    // нужно сделать чтобы поток и в нём обновлялось проигрываемое значение
//    inner class PlayerPosition() : Runnable {
//        override fun run() {
//            while (mediaSession.controller.playbackState.state != PlaybackStateCompat.STATE_NONE ||
//                mediaSession.controller.playbackState.state != PlaybackStateCompat.STATE_STOPPED ||
//                mediaSession.controller.playbackState.state != PlaybackStateCompat.STATE_ERROR
//            ) {
//                val state = mediaSession.controller.playbackState.state
//                setMediaPlaybackState(state)
//                handler.postDelayed(this, 900L)
//            }
//        }
//    }

    override fun onCreate() {
        super.onCreate()

        init()
    }

    private fun init() {
        Log.i(TAG, "init: 321")
        initMediaSession()
        initPlayer()
//        initReceiver()
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(applicationContext, "MediaPlaybackService").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS or
                        MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
            )
            setCallback(callback)
            setSessionToken(this.sessionToken)
            val builder = PlaybackStateCompat
                .Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_STOP or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                )

            setPlaybackState(builder.build())

            val intent = Intent(applicationContext, MainActivity::class.java)
            setSessionActivity(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }

    private fun initPlayer() {
        player = Player(applicationContext)
        Log.i(TAG, "initPlayer: 123")
        player.onCompletionListener = onCompletionListener
    }

//    private fun initReceiver() {
//        noisyReceiver = NoisyReceiver(mediaSession)
//    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: asddas")
        intent?.action?.let {
            when (it) {
                "play" -> callback.onPlay()
                "pause" -> callback.onPause()
                "next" -> callback.onSkipToNext()
                "previous" -> callback.onSkipToPrevious()
            }
        }
        Log.i(TAG, "onStartCommand: 123")
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        player.stop()
//        handler.removeCallbacks(playerPosition)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        if (TextUtils.equals(packageName, clientPackageName)) {
            Log.i(TAG, "onGetRoot: aee")
            return BrowserRoot(getString(R.string.app_name), null)
        } else {
            Log.i(TAG, "onGetRoot: oh nooo")
            return BrowserRoot(getString(R.string.on_get_root), null)
        }
    }


    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(null)
    }


    companion object {
        const val TAG = "PLAYBACK_SERVICE"

        @Pashalko
        val NOTIFIC_CHANNEL_ID = 1488
    }
}
