package com.example.mediaservice.adapter

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaservice.Audio
import com.example.mediaservice.R
import com.example.mediaservice.databinding.AudioItemBinding
import com.squareup.picasso.Picasso
import java.lang.RuntimeException

class AudioAdapter : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    var list = ArrayList<Audio>()
    val picasso = Picasso.get()
    var pos = -1

    lateinit var onItemClick: (Audio) -> Unit
    private lateinit var lastTrack: AudioItemBinding

    inner class AudioViewHolder(val item: View) : RecyclerView.ViewHolder(item) {
        val binding = AudioItemBinding.bind(item)

        init {
            item.setOnClickListener {
                if (::lastTrack.isInitialized || (pos != -1 && pos != bindingAdapterPosition) ) {
                    Log.i(TAG, "pos: $pos")
                    Log.i(TAG, "${pos != -1 && pos != layoutPosition}: ")
                    clearGrayFilter()
                }
                pos = adapterPosition
                lastTrack = binding
                onItemClick.invoke(list[pos])

                setGrayFilter()
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(audio: Audio) {
            binding.audioName.text = audio.name
            binding.authorName.text = audio.artist
            binding.duration.text = "${audio.duration / 60000}:${(audio.duration % 60000) / 1000}"

            if (audio.art_uri == null) setDefaultIcon()
            else {
                setIcon(audio.art_uri)
            }
        }

        private fun setGrayFilter() {
            binding.icon.setColorFilter(R.color.black, PorterDuff.Mode.SRC_OVER)

        }

        private fun clearGrayFilter() {
            lastTrack.icon.clearColorFilter()
        }

        private fun setIcon(uri: Uri) {
            Log.i(TAG, "setIcon: $uri")
            picasso.load(uri)
                .error(R.drawable.flowers)
                .into(binding.icon)

        }

        private fun setDefaultIcon() {
            picasso
                .load(R.drawable.flowers)
                .into(binding.icon)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.audio_item, parent, false)
        return AudioViewHolder(itemView)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        Log.i(TAG, "onBindViewHolder: $position")
        holder.bind(list[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setAudioList(_list: ArrayList<Audio>) {
        list = _list
        notifyDataSetChanged()

        if (list.size == 0) {
            throw RuntimeException("asd")
        }
    }

    companion object {
        const val TAG = "AUDIO_ADAPTER"
    }
}
