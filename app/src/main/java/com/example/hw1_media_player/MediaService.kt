package com.example.hw1_media_player

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.session.MediaSessionManager
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class MediaService : Service() {

    private val binder = LocalBinder()

    lateinit var mediaPlayer: MediaPlayer
    var numberSongPlayed = 0
    lateinit var playlist: List<Uri>
    var isPlaying = false

    private val NOTIFICATION_ID = 101

    lateinit var mediaSessionManager: MediaSessionManager
    lateinit var mediaSession: MediaSessionCompat
    lateinit var transportControls: MediaControllerCompat.TransportControls


    inner class LocalBinder : Binder() {
        fun getService(): MediaService = this@MediaService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        playlist = makePlaylist()
        mediaPlayer = MediaPlayer.create(this, playlist[0])
        initMediaSession()
        showNotification(PlaybackStatus.PAUSED)
        handleActions(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    fun showNotification(playbackStatus: PlaybackStatus) {
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(NOTIFICATION_ID, createNotification(playbackStatus))
        }
    }

    private fun createNotification(playbackStatus: PlaybackStatus): Notification {

        val builder = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .addAction(R.drawable.baseline_skip_previous_24, "previous", playbackAction(3))
            .addAction(R.drawable.baseline_play_circle_outline_24, "play", playbackAction(0))
            .addAction(R.drawable.baseline_pause_circle_outline_24, "pause", playbackAction(1))
            .addAction(R.drawable.baseline_skip_next_24, "next", playbackAction(2))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2, 3)
            )
            .setContentTitle("Media Player")
            .setSmallIcon(R.drawable.baseline_music_note_24)

        return builder.build()
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackActionIntent = Intent(this, MediaService::class.java)
        when (actionNumber) {
            0 -> {
                playbackActionIntent.action = Constants.ACTION.ACTION_PLAY
                return PendingIntent.getService(
                    this, actionNumber, playbackActionIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

            1 -> {
                playbackActionIntent.action = Constants.ACTION.ACTION_PAUSE
                return PendingIntent.getService(
                    this, actionNumber, playbackActionIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

            2 -> {
                playbackActionIntent.action = Constants.ACTION.ACTION_NEXT
                return PendingIntent.getService(
                    this, actionNumber, playbackActionIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

            3 -> {
                playbackActionIntent.action = Constants.ACTION.ACTION_PREVIOUS
                return PendingIntent.getService(
                    this, actionNumber, playbackActionIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
        }
        return null
    }


    fun initMediaSession() {
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        mediaSession = MediaSessionCompat(this, "Media Player")
        transportControls = mediaSession.controller.transportControls
        mediaSession.isActive = true

        mediaSession.setCallback(
            object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    super.onPlay()
                    startSong()
                    showNotification(PlaybackStatus.PLAYING)
                }

                override fun onPause() {
                    super.onPause()
                    pauseSong()
                    showNotification(PlaybackStatus.PAUSED)
                }

                override fun onSkipToNext() {
                    super.onSkipToNext()
                    nextSong()
                    showNotification(PlaybackStatus.PLAYING)
                }

                override fun onSkipToPrevious() {
                    super.onSkipToPrevious()
                    previousSong()
                    showNotification(PlaybackStatus.PLAYING)
                }
            }
        )
    }

    fun handleActions(playbackAction: Intent?) {
        if (playbackAction != null) {
            val action = playbackAction.action
            when (action) {
                Constants.ACTION.ACTION_PLAY -> transportControls.play()
                Constants.ACTION.ACTION_NEXT -> transportControls.skipToNext()
                Constants.ACTION.ACTION_PREVIOUS -> transportControls.skipToPrevious()
                Constants.ACTION.ACTION_PAUSE -> transportControls.pause()
            }
        }

    }

    fun startSong() {
        if (!isPlaying) {
            mediaPlayer.start()
            isPlaying = true
        }
    }

    fun pauseSong() {
        if (isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
        }
    }

    fun nextSong() {
        numberSongPlayed = (numberSongPlayed + 1) % 3
        mediaPlayer.reset()
        mediaPlayer.setDataSource(this, playlist[numberSongPlayed])
        mediaPlayer.prepare()
        mediaPlayer.start()
        isPlaying = true

    }

    fun previousSong() {
        numberSongPlayed =
            if (numberSongPlayed == 0) playlist.size - 1 else numberSongPlayed - 1
        mediaPlayer.reset()
        mediaPlayer.setDataSource(this, playlist[numberSongPlayed])
        mediaPlayer.prepare()
        mediaPlayer.start()
        isPlaying = true
    }


    fun makePlaylist(): List<Uri> {
        return mutableListOf(
            uriFromRaw(R.raw.bitter_pill),
            uriFromRaw(R.raw.save_me_now),
            uriFromRaw(R.raw.cant_break_me_down)
        )
    }

    private fun uriFromRaw(resource: Int): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(packageName)
            .path(resource.toString())
            .build()
    }

}


