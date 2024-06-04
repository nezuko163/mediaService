package com.example.mediaservice.fragment

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.mediaservice.R
import com.example.mediaservice.adapter.CurrentPlayingTrackAdapter
import com.example.mediaservice.databinding.CurrentPlayingTrackBinding
import com.example.mediaservice.service.TransportControlInterface
import com.example.mediaservice.viewmodels.AudioViewModel
import com.example.mediaservice.viewmodels.PlayBackViewModel
import com.example.mediaservice.viewmodels.PlayQueueViewModel

class CurrentPlayingTrackFragment(val transportCallback: TransportControlInterface) :
    Fragment(R.layout.current_playing_track) {
    private val TAG = "CURRENT_FRAGMENT"
    private lateinit var binding: CurrentPlayingTrackBinding
    private lateinit var adapter: CurrentPlayingTrackAdapter

    private val metadataViewModel: AudioViewModel by activityViewModels()
    private val playBackViewModel: PlayBackViewModel by activityViewModels()
    private val playQueueViewModel: PlayQueueViewModel by activityViewModels()

    private var isPlaying = false
    private var flag = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CurrentPlayingTrackBinding.inflate(layoutInflater)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.root.visibility = View.GONE
        return binding.root
    }

    private fun init() {
        adapter = CurrentPlayingTrackAdapter()
        adapter.onItemClick = { replaceFragment() }
        binding.audioDetails.adapter = adapter
        binding.audioDetails.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.i(TAG, "onPageSelected: $position")
                Log.i(TAG, "onPageSelected: flag = $flag")
                if (flag) {
                    Log.i(TAG, "onPageSelected: skipTo")
                    transportCallback.skipTo(position)
                }
                flag = true
            }
        })

        binding.btn.setOnClickListener {
            if (isPlaying) transportCallback.pause()
            else transportCallback.play()
        }



        metadataViewModel.selectedMetadata.observe(viewLifecycleOwner) {
            val id = it.bundle.getLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS).toInt()
            Log.i(TAG, "metadata changed: $id")
            binding.audioDetails.doOnLayout {
                flag = false
                binding.audioDetails.setCurrentItem(id, false)
            }
            Log.i(TAG, "metadata changed: flag = $flag")
        }

        playBackViewModel.playbackViewModel.observe(viewLifecycleOwner) {
//            if (view?.visibility == View.GONE) {
//                if (!flag) {
//                    flag = true
//                }
//                else {
//                    Log.i(TAG, "init: 123")
//                    view?.visibility = View.VISIBLE
//                }
//            }
            view?.visibility = View.VISIBLE
            isPlaying = it.state == PlaybackStateCompat.STATE_PLAYING
            changePlayPauseIcon(isPlaying)
        }

        playQueueViewModel.playQueueViewModel.observe(viewLifecycleOwner) {
            Log.i(TAG, "playqueue changed")
            adapter.list = it
        }
    }

    private fun replaceFragment() {
        parentFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
            setReorderingAllowed(true)
            replace(R.id.container, ControlPlayingTrackFragment::class.java, null)
        }
    }


    private fun changePlayPauseIcon(isPlaying: Boolean) {
        val res = if (isPlaying) R.drawable.pause else R.drawable.play

        binding.btn.setImageResource(res)
    }
}