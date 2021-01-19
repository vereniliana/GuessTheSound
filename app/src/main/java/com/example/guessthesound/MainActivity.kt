package com.example.guessthesound

import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_play.setOnClickListener {
            mediaPlayer?.start()
            startActivity(Intent(this, GameActivity::class.java))
        }

        mediaPlayer?.setOnCompletionListener {
            it.release()
        }
    }

    override fun onStart() {
        super.onStart()
        mediaPlayer = MediaPlayer.create(this, R.raw.button)
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
    }
}