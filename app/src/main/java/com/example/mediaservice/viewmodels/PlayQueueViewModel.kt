package com.example.mediaservice.viewmodels

import com.example.mediaservice.Audio
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayQueueViewModel : ViewModel() {
    private val _playQueueViewMovel = MutableLiveData<ArrayList<Audio>>()
    val playQueueViewModel: LiveData<ArrayList<Audio>>
        get() = _playQueueViewMovel

    fun set(playQueue: ArrayList<Audio>) {
        _playQueueViewMovel.value = playQueue
    }
}