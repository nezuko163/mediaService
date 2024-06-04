package com.example.mediaservice.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaservice.Audio
import com.example.mediaservice.R
import com.example.mediaservice.databinding.AudioItemBinding
import com.squareup.picasso.Picasso
import java.lang.RuntimeException

class AudioAdapter(
    private val context: Context,
    val listener: OptionsMenuClickListener
) :
    RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    var list = ArrayList<Audio>()
    val picasso = Picasso.get()
    var pos = -1

    lateinit var onItemClick: (Audio) -> Unit
    private lateinit var lastTrack: AudioViewHolder

    inner class AudioViewHolder(val item: View) : RecyclerView.ViewHolder(item) {
        val binding = AudioItemBinding.bind(item)

        init {
            item.setOnClickListener {
                if (::lastTrack.isInitialized || (pos != -1 && pos != bindingAdapterPosition)) {
                    Log.i(TAG, "pos: $pos")
                    Log.i(TAG, "${pos != -1 && pos != layoutPosition}: ")
                    clearGrayFilter()
                }
                pos = bindingAdapterPosition
                lastTrack = this
                onItemClick.invoke(list[pos])

                setGrayFilter()
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(audio: Audio) {
            val popupMenu = PopupMenu(context, binding.dots)
            popupMenu.inflate(R.menu.track_menu)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.add_to_queue -> {
                        listener.addToQueue(bindingAdapterPosition)
                        true
                    }

                    R.id.add_to_playlist -> {
                        listener.addToPlaylist(bindingAdapterPosition)
                        true
                    }

                    R.id.change_credits -> {
                        listener.changeCredits(bindingAdapterPosition)
                        true
                    }

                    else -> false
                }
            }

            binding.audioName.text = audio.name
            binding.authorName.text = audio.artist

            binding.dots.setOnClickListener {
                popupMenu.show()
            }

            if (audio.art_uri == null) setDefaultIcon()
            else {
                setIcon(audio.art_uri)
            }
        }

        fun setGrayFilter() {
            binding.icon.setColorFilter(R.color.black, PorterDuff.Mode.SRC_OVER)

        }

        fun clearGrayFilter() {
            lastTrack.binding.icon.clearColorFilter()
        }

        private fun setIcon(uri: Uri) {
            Log.i(TAG, "setIcon: $uri")
            picasso
                .load(uri)
                .error(R.drawable.musique_94037)
                .into(binding.icon)

        }

        private fun setDefaultIcon() {
            picasso
                .load(R.drawable.flowers)
                .error(R.drawable.musique_94037)
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

    fun trackChanged(audio: AudioViewHolder, id: Long) {
        pos = id.toInt()
        lastTrack.clearGrayFilter()

        lastTrack = audio
        lastTrack.setGrayFilter()

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
