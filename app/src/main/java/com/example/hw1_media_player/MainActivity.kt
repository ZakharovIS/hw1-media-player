package com.example.hw1_media_player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.example.hw1_media_player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mService: MediaService
    var mBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = p1 as MediaService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!mBound) {
            val intent = Intent(this, MediaService::class.java)
            startService(intent)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        binding.imageButtonPlayPause.setOnClickListener {
            if (mService.isPlaying) {
                mService.pauseSong()
            } else {
                mService.startSong()
            }
            refreshButtonPlayIcon()
        }

        binding.imageButtonNext.setOnClickListener {
            mService.nextSong()
            refreshButtonPlayIcon()
        }

        binding.imageButtonPrevious.setOnClickListener {
            mService.previousSong()
            refreshButtonPlayIcon()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        mBound = false
    }

    fun refreshButtonPlayIcon() {
        if (mService.isPlaying) binding.imageButtonPlayPause.setImageResource(R.drawable.pause_circle)
        else binding.imageButtonPlayPause.setImageResource(R.drawable.play_circle)
    }

}