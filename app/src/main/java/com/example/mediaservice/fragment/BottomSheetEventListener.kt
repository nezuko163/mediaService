package com.example.mediaservice.fragment

import android.view.View

interface BottomSheetEventListener {
    fun onStateChanged(p0: View, p1: Int)
    fun onSlide(p0: View, p1: Float)
}