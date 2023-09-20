package com.example.hw1_media_player

import android.content.ContentResolver
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hw1_media_player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var numberSongPlayed = 0

        var mediaPlayer = MediaPlayer.create(this, R.raw.bitter_pill)
        var playList = makePlaylist()

        binding.imageButtonPlayPause.setOnClickListener {

            mediaPlayer.start()

        }

        binding.imageButtonNext.setOnClickListener {

            numberSongPlayed = (numberSongPlayed + 1) % 3
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, playList[numberSongPlayed])
            mediaPlayer.prepare()
            mediaPlayer.start()
        }

        binding.imageButtonPrevious.setOnClickListener {

            numberSongPlayed = if (numberSongPlayed == 0) playList.size - 1 else numberSongPlayed - 1
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, playList[numberSongPlayed])
            mediaPlayer.prepare()
            mediaPlayer.start()
        }

    }


    fun makePlaylist(): List<Uri> {
        return mutableListOf<Uri>(
            uriFromRaw(R.raw.bitter_pill),
            uriFromRaw(R.raw.save_me_now),
            uriFromRaw(R.raw.cant_break_me_down)
        )
    }
    fun uriFromRaw(resource: Int): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(packageName)
            .path(resource.toString())
            .build()
    }

}