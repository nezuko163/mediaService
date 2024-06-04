package com.example.mediaservice.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.example.mediaservice.R
import com.example.mediaservice.activity.MainActivity


class NotificationHelper {
    companion object {
        private val TAG = "NOTIFIC_HELP"

        fun notificationBuilder(
            context: Context,
            mediaSession: MediaSessionCompat,
            isPlaying: Boolean,
            channelId: String = context.getString(R.string.NOTIFICATION_CHANNEL_ID)
        ): NotificationCompat.Builder {
            val controller = mediaSession.controller
            val mediaMetadata = controller.metadata
            val description = mediaMetadata.description

            val builder = NotificationCompat.Builder(context, channelId).apply {

                // Add the metadata for the currently playing track
                setContentTitle(description.title)
                setContentText(description.subtitle)
                setSubText(description.description)

                val bitmap = Tool.getBitmapFromUri(description.mediaUri, context)
                setLargeIcon(bitmap)
                setSmallIcon(R.drawable.ic_launcher_foreground)

//                setSubText(description.description)

                // Enable launching the player by clicking the notification

                val intent = context.packageManager
                    .getLaunchIntentForPackage(context.packageName)
                    ?.setPackage(null)
                    ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                val contentIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                setContentIntent(contentIntent)

                // Stop the service when the notification is swiped away
                setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )

                // Make the transport controls visible on the lockscreen
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Add an app icon and set its accent color
                // Be careful about the color
                val color = ContextCompat.getColor(context, R.color.cyan)
                setColor(color)


                // Add a skip to previous button
                addAction(
                    NotificationCompat.Action(
                        R.drawable.baseline_fast_rewind_24,
                        context.getString(R.string.previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                    )
                )
                // Add a pause button
                addAction(
                    NotificationCompat.Action(
                        if (isPlaying) R.drawable.pause else R.drawable.play,
                        context.getString(if (isPlaying) R.string.pause else R.string.play),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                        )
                    )
                )

                // Add skip to next button
                addAction(
                    NotificationCompat.Action(
                        R.drawable.baseline_fast_forward_24,
                        context.getString(R.string.next),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        )
                    )
                )

                // Take advantage of MediaStyle features
                setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)

                        // Add a cancel button
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context,
                                PlaybackStateCompat.ACTION_STOP
                            )
                        )
                )
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                setPriority(NotificationCompat.PRIORITY_HIGH)
            }
//            builder.setOnlyAlertOnce(true)
            return builder
        }

        fun createChannelForMediaPlayerNotification(context: Context) {
            val channel = NotificationChannel(
                context.getString(R.string.NOTIFICATION_CHANNEL_ID),
                context.getString(R.string.NOTIFICATION_CHANNEL_ID),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "All app notifications"
                setSound(null, null)
                setShowBadge(false)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.i(
                "MAIN_ACTIVITY",
                "createChannelForMediaPlayerNotification: ${notificationManager.areNotificationsEnabled()}"
            )
        }
    }
}