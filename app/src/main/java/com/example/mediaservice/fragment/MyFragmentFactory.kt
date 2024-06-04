package com.example.mediaservice.fragment

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.example.mediaservice.service.TransportControlInterface

class MyFragmentFactory(val transportControl: TransportControlInterface) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        Log.i("MAIN_ACTIVITY", "instantiate: $className")
        return when(className) {
            CurrentPlayingTrackFragment::class.java.name -> CurrentPlayingTrackFragment(transportControl)
            ControlPlayingTrackFragment::class.java.name -> ControlPlayingTrackFragment(transportControl)
            else -> super.instantiate(classLoader, className)
        }
    }
}