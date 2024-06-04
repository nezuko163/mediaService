package com.example.mediaservice.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mediaservice.Audio
import com.example.mediaservice.R
import com.example.mediaservice.databinding.ControlPlayingViewholderBinding

class ControlPlayingTrackAdapter :
    RecyclerView.Adapter<ControlPlayingTrackAdapter.ControlPlayingTrackViewholder>() {

    private val TAG = "CONTROLADAPTER"
    var list = ArrayList<Audio>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            notifyDataSetChanged()
            field = value
        }
    var index = 0


    inner class ControlPlayingTrackViewholder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding = ControlPlayingViewholderBinding.bind(itemView)

        fun bind(audio: Audio, position: Int) {
            Glide
                .with(itemView.context)
                .load(audio.art_uri)
                .error(R.drawable.musique_94037)
                .into(binding.icon)
            Log.i(TAG, "bind: ${audio.art_uri}")
            itemView.id = position
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ControlPlayingTrackViewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.control_playing_viewholder, parent, false)
        return ControlPlayingTrackViewholder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ControlPlayingTrackViewholder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemId(position: Int): Long {
        Log.i(TAG, "getItemId: pos = $position")
        return position.toLong()
    }
}