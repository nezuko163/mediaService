package com.example.mediaservice.viewmodels

import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayBackViewModel : ViewModel() {
    private val _playbackViewModel = MutableLiveData<PlaybackStateCompat>()
    val playbackViewModel: LiveData<PlaybackStateCompat>
        get() = _playbackViewModel

    fun setPlaying(playbackViewModel: PlaybackStateCompat) {
        _playbackViewModel.value = playbackViewModel
    }
}