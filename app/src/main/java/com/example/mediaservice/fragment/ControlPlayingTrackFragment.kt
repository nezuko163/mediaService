package com.example.mediaservice.fragment

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.mediaservice.R
import com.example.mediaservice.adapter.ControlPlayingTrackAdapter
import com.example.mediaservice.databinding.ControlPlayingTrackFragmentBinding
import com.example.mediaservice.service.TransportControlInterface
import com.example.mediaservice.viewmodels.AudioViewModel
import com.example.mediaservice.viewmodels.PlayBackViewModel
import com.example.mediaservice.viewmodels.PlayQueueViewModel
import com.google.android.material.slider.Slider
import com.google.android.material.slider.Slider.OnSliderTouchListener

class ControlPlayingTrackFragment(val transportCallback: TransportControlInterface) :
    Fragment(R.layout.control_playing_track_fragment) {

    private val TAG = "CONTROL_TRACK_FRAGMENT"
    private lateinit var binding: ControlPlayingTrackFragmentBinding
    private lateinit var adapter: ControlPlayingTrackAdapter

    private val playQueueViewModel: PlayQueueViewModel by activityViewModels()
    private val metadataViewModel: AudioViewModel by activityViewModels()
    private val playBackViewModel: PlayBackViewModel by activityViewModels()
    private var isPlaying = false
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ControlPlayingTrackFragmentBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initUi()
        initViewModels()


    }

    private fun initViewModels() {
        metadataViewModel.selectedMetadata.observe(viewLifecycleOwner) {
            bindTrack(it)
            val id = it.bundle.getLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS).toInt()

            binding.icon.postDelayed({
                binding.icon.currentItem = id
            }, 100)
            Log.i(TAG, "metadata observe: currentItem = ${binding.icon.currentItem}")
        }

        playBackViewModel.playbackViewModel.observe(viewLifecycleOwner) {
            isPlaying = it.state == PlaybackStateCompat.STATE_PLAYING
            changePlayPauseIcon(isPlaying)
            updatePlayerPosition(it.position)
        }

        playQueueViewModel.playQueueViewModel.observe(viewLifecycleOwner) {
            Log.i(TAG, "initViewModels: play queue changed")
            adapter.list = it
        }
    }

    private fun initUi() {
        adapter = ControlPlayingTrackAdapter()
        binding.apply {
            icon.setOnClickListener {
            }

            play.setOnClickListener {
                if (isPlaying) transportCallback.pause()
                else transportCallback.play()
            }

            previous.setOnClickListener {
                transportCallback.skipToPrevious()
            }

            next.setOnClickListener {
                transportCallback.skipToNext()
            }

            arrowImg.setOnClickListener {
                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.container, CurrentPlayingTrackFragment::class.java, null)
                }
            }

            slide.addOnSliderTouchListener(object : OnSliderTouchListener {
                override fun onStartTrackingTouch(p0: Slider) {

                }

                override fun onStopTrackingTouch(p0: Slider) {
                    transportCallback.seekTo(p0.value.toLong())
                }

            })
            binding.icon.adapter = adapter
            binding.icon.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    Log.i(TAG, "onPageSelected: scrolled to pos=$position")

                    if (count >= 2) {
                        transportCallback.skipTo(position)
                    }
                    count++
                    super.onPageSelected(position)
                }
            })

            Log.i(TAG, "initUi: view pager end")
        }
    }

    fun bindTrack(metadata: MediaMetadataCompat) {
        val duration = metadata.bundle.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) ?: return
        binding.slide.valueTo = duration.toFloat()
        val secs = (duration / 1000).toInt()
        binding.apply {
            author.text = metadata.description.subtitle
            name.text = metadata.description.title
            timeEnd.text = setTime(secs)

        }
    }

    fun setTime(sec: Int): String {
        val min: Int = sec / 60
        val secq = sec % 60
        var secs = "$secq"
        secs = "0".repeat(2 - secs.length) + secs
        return "$min:$secs"
    }

    fun changePlayPauseIcon(isPlaying: Boolean) {
        val res = if (isPlaying) R.drawable.pause else R.drawable.play

        binding.play.setImageResource(res)
    }

    fun updatePlayerPosition(ms: Long) {
        binding.slide.value = ms.toFloat()

        binding.timeNow.text = setTime((ms / 1000).toInt())
    }
}