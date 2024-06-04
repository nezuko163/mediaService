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
import com.example.mediaservice.databinding.CurrentPlayingTrackBinding
import com.example.mediaservice.databinding.CurrentPlayingViewholderBinding
import com.example.mediaservice.viewmodels.AudioViewModel
import com.example.mediaservice.viewmodels.PlayQueueViewModel

class CurrentPlayingTrackAdapter :
    RecyclerView.Adapter<CurrentPlayingTrackAdapter.CurrentPlayingTrackViewHolder>() {

    lateinit var onItemClick: () -> Unit

    val TAG = "CURRENT_ADAPTER"
    var list = ArrayList<Audio>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            notifyDataSetChanged()
            Log.i(TAG, "setList: $value")
            field = value
        }


    inner class CurrentPlayingTrackViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding = CurrentPlayingViewholderBinding.bind(itemView)

        init {
            itemView.setOnClickListener {
                onItemClick.invoke()
            }
        }

        fun bind(audio: Audio, position: Int) {
            bindCurrentTrack(audio)
            itemView.id = position
        }

        private fun bindCurrentTrack(audio: Audio) {
            Glide
                .with(itemView.context)
                .load(audio.art_uri)
                .error(R.drawable.musique_94037)
                .centerCrop()
                .into(binding.img)
            binding.audioName.text = audio.name
            binding.authorName.text = audio.artist
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CurrentPlayingTrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.current_playing_viewholder, parent, false)
        return CurrentPlayingTrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: CurrentPlayingTrackViewHolder, position: Int) {
        Log.i(TAG, "onBindViewHolder: ")
        holder.bind(list[position], position)
    }

    override fun getItemCount() = list.size

    override fun getItemId(position: Int): Long {
        Log.i(TAG, "getItemId: pos = $position")
        return position.toLong()
    }
}