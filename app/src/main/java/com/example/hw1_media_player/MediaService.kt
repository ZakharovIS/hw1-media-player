package com.example.hw1_media_player

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder

class MediaService : Service() {

    private val binder = LocalBinder()

    lateinit var mediaPlayer: MediaPlayer
    var numberSongPlayed = 0
    lateinit var playlist : List<Uri>
    var isPlaying = false

    private val NOTIFICATION_ID = 1


    inner class LocalBinder : Binder() {
        fun getService(): MediaService = this@MediaService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        playlist = makePlaylist()
        mediaPlayer = MediaPlayer.create(this, playlist[0])
        return super.onStartCommand(intent, flags, startId)
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


