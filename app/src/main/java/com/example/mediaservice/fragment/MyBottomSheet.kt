package com.example.mediaservice.fragment

import android.util.Log
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MyBottomSheet(view: View, listener: BottomSheetEventListener) {
    private val TAG = "BOTTOM_SHEET"
    private val btmSheet = BottomSheetBehavior.from(view)
    var lastState = 0

    init {
        btmSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(p0: View, p1: Int) {
                listener.onStateChanged(p0, p1)
                lastState = p1
                Log.i(TAG, "onStateChanged: $lastState")
            }

            override fun onSlide(p0: View, p1: Float) {
//                Log.i(TAG, "onSlide: $p1")
                listener.onSlide(p0, p1)
            }

        })
    }


}