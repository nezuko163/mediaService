package com.example.mediaservice.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaDescription
import android.media.MediaMetadata
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat.QueueItem
import android.util.Log
import com.example.mediaservice.Audio
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class Tool {
    companion object {
        fun saveBitmapToTemporaryFile(bitmap: Bitmap): File? {
            return try {
                val file = File.createTempFile("temp", ".jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                file
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        fun resIdToUri(context: Context, resId: Int): Uri {
            val uri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(context.resources.getResourcePackageName(resId))
                .appendPath(context.resources.getResourceTypeName(resId))
                .appendPath(context.resources.getResourceEntryName(resId))
                .build()

            return uri
        }

        fun metadataBuilder(
            audio: Audio?,
            id: Long = 0L
        ): MediaMetadataCompat.Builder? {
            if (audio == null) return null

            val builder = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, audio.art_uri.toString())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audio.name)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, audio.album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, audio.artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, audio.duration)
                .putLong("queue_id", id)
            return builder
        }

        fun metadataBuilder(queueItem: QueueItem?): MediaMetadataCompat.Builder? {
            if (queueItem == null) return null

            val duration = queueItem.description.extras?.getLong("duration") ?: return null
            val id = queueItem.description.extras?.getLong("queue_id") ?: return null

            Log.i("MAIN_ACTIVITY", "metadataBuilder: $duration")

            val builder = MediaMetadataCompat.Builder()
                .putString(
                    MediaMetadataCompat.METADATA_KEY_ART_URI,
                    queueItem.description.iconUri.toString()
                )
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                    queueItem.description.mediaUri.toString())
                .putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    queueItem.description.title.toString()
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_ARTIST,
                    queueItem.description.subtitle.toString()
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,
                    queueItem.description.description.toString()
                )
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, id)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
            return builder
        }

        fun getBitmapFromUri(uri: Uri?, context: Context): Bitmap? {
            var inputStream: InputStream? = null
            try {
                inputStream = uri?.let { context.contentResolver.openInputStream(it) }
                return BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                inputStream?.close()
            }
            return null
        }

        fun audioToMediaDescriptionCompat(audio: Audio, id: Long? = null): MediaDescriptionCompat {
            val bundle = Bundle().apply {
                putLong("duration", audio.duration)
                if (id != null) {
                    putLong("queue_id", id)
                }
            }
            Log.i("ART_URI", "audioToMediaDescriptionCompat: ${audio.art_uri}")
            Log.i("MEDIA_URI", "audioToMediaDescriptionCompat: ${audio.audio_uri}")
            return MediaDescriptionCompat.Builder()
                .setExtras(bundle)
                .setTitle(audio.name)
                .setSubtitle(audio.artist)
                .setMediaUri(audio.audio_uri)
                .setIconUri(audio.art_uri)
                .setDescription(audio.name)
                .build()
        }
    }
}