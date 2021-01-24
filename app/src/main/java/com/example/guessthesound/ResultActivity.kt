package com.example.guessthesound

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val gameResult = intent.getIntExtra(GAME_RESULT_KEY, 0)
        val numQuestions = intent.getIntExtra(NUM_QUESTIONS, 2)
        val mediaPlayer = MediaPlayer.create(this, R.raw.button)

        if (gameResult == 0) {
            tv_win.visibility = View.GONE
            tv_lose.visibility = View.VISIBLE
            btn_play_again.text = resources.getString(R.string.text_try_again)
        } else {
            tv_lose.visibility = View.GONE
            tv_win.visibility = View.VISIBLE
            btn_play_again.text = resources.getString(R.string.text_play_again)
        }

        btn_play_again.setOnClickListener {
            mediaPlayer.start()
            startActivity(
                Intent(this, LoadingActivity::class.java)
                    .putExtra(NUM_QUESTIONS, numQuestions)
            )
            finish()
        }

        mediaPlayer.setOnCompletionListener {
            it.release()
        }
    }
}