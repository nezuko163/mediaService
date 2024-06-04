package com.example.mediaservice.adapter

interface OptionsMenuClickListener {
    fun addToQueue(position: Int)
    fun addToPlaylist(position: Int)
    fun changeCredits(position: Int)
}