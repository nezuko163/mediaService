package com.example.mediaservice.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.CodeBoy.MediaFacer.AudioGet
import com.CodeBoy.MediaFacer.MediaFacer
import com.example.mediaservice.Audio

class AllAudios {
    companion object {
        private val TAG = "ALL_AUDIOS"

        fun getAudios(context: Context): ArrayList<Audio> {
            val lst_audio = ArrayList<Audio>()

            MediaFacer.withAudioContex(context)
                .getAllAudioContent(AudioGet.externalContentUri)
                .forEach {
                    if (it.duration == 0L) return@forEach
                    val audio = Audio(
                        it.filePath,
                        it.name,
                        it.album,
                        it.artist,
                        it.duration,
                        it.date_taken,
                        it.art_uri,
                        Uri.parse(it.assetFileStringUri),
                    )
                    Log.i(TAG, "getAudios: ${it.name}")

                    lst_audio.add(audio)
                }
            return lst_audio
        }

    }
}