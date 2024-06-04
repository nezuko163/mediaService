package com.example.mediaservice.viewmodels

import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AudioViewModel : ViewModel() {
    private val _selectedMetadata = MutableLiveData<MediaMetadataCompat>()
    val selectedMetadata: LiveData<MediaMetadataCompat>
        get() = _selectedMetadata

    fun selectMetadata(metadata: MediaMetadataCompat) {
        _selectedMetadata.value = metadata
    }
}