package com.example.mediaservice.service

interface TransportControlInterface {
    fun skipToNext()
    fun skipToPrevious()
    fun skipTo(id: Int)
    fun seekTo(ms: Long)
    fun play()
    fun pause()
    fun stop()
}