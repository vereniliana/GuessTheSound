package com.example.guessthesound

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

const val NUM_QUESTIONS = "num_questions"

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_play.setOnClickListener {
            mediaPlayer?.start()
            val numQuestions = et_num_questions.text.toString().toInt()
            startActivity(
                Intent(this, LoadingActivity::class.java)
                    .putExtra(NUM_QUESTIONS, numQuestions)
            )
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