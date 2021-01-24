package com.example.guessthesound

import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_game.*

const val GAME_RESULT_KEY = "game_result"

class GameActivity : AppCompatActivity() {

    lateinit var currentQuestion: Question
    private var questionIndex = 0
    private var numQuestions = 2
    private lateinit var questions: ArrayList<Question>

    private var isBacksoundMute = false

    private lateinit var questionPlayer: MediaPlayer
    private var isQuestionPrepared: Boolean = false

    private lateinit var buttonSfx: MediaPlayer
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        questions = intent.getParcelableArrayListExtra(GAME_QUESTIONS) ?: ArrayList()
        numQuestions = questions.size

        questionPlayer = createPlayer()

        buttonSfx = MediaPlayer.create(this, R.raw.button)

        setQuestion()

        btn_play_question.setOnClickListener {
            if (isQuestionPrepared) {
                showPauseButton()
                BacksoundManager.pause()
                questionPlayer.start()
                updateTime.run()
                updateProgress.run()
            }
        }

        btn_pause_question.setOnClickListener {
            if (questionPlayer.isPlaying) {
                showPlayButton()
                questionPlayer.pause()
                if (!isBacksoundMute) {
                    BacksoundManager.play()
                }
            }
        }

        btn_mute.setOnClickListener {
            isBacksoundMute = true
            showUnmuteButton()
            BacksoundManager.pause()
        }

        btn_unmute.setOnClickListener {
            isBacksoundMute = false
            showMuteButton()
            if (!isBacksoundMute) {
                BacksoundManager.play()
            }
        }

        btn_submit.setOnClickListener {
            buttonSfx.start()
            if (et_answer.text.toString() == currentQuestion.answer) {
                questionIndex++
                if (questionIndex < numQuestions) {
                    resetUI()
                    setQuestion()
                } else {
                    startActivity(
                        Intent(this, ResultActivity::class.java)
                            .putExtra(GAME_RESULT_KEY, 1)
                            .putExtra(NUM_QUESTIONS, numQuestions)
                    )
                    finish()
                }
            } else {
                startActivity(
                    Intent(this, ResultActivity::class.java)
                        .putExtra(GAME_RESULT_KEY, 0)
                        .putExtra(NUM_QUESTIONS, numQuestions)
                )
                finish()
            }
        }

    }

    private val updateTime: Runnable = object : Runnable {
        override fun run() {
            val currentDuration: Int
            if (questionPlayer.isPlaying) {
                currentDuration = questionPlayer.currentPosition
                tv_currentTime.text = milliSecondsToTimer(currentDuration)
                handler.postDelayed(this, 500)
            } else {
                handler.removeCallbacks(this)
            }
        }
    }

    private val updateProgress: Runnable = object : Runnable {
        override fun run() {
            val currentDuration: Int
            if (questionPlayer.isPlaying) {
                currentDuration = questionPlayer.currentPosition
                pb_player_timer.progress = currentDuration
                handler.postDelayed(this, 1000)
            } else {
                handler.removeCallbacks(this)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isBacksoundMute) {
            BacksoundManager.play()
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateTime)
        handler.removeCallbacks(updateProgress)
        BacksoundManager.pause()
        pausePlayer(questionPlayer)
    }

    override fun onDestroy() {
        BacksoundManager.release()
        releasePlayer(questionPlayer)
        releasePlayer(buttonSfx)
        super.onDestroy()
    }

    private fun createPlayer(): MediaPlayer {
        return MediaPlayer().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            } else {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
        }
    }

    private fun pausePlayer(mp: MediaPlayer) {
        if (mp.isPlaying) {
            mp.pause()
        }
    }

    private fun releasePlayer(mp: MediaPlayer) {
        if (mp.isPlaying) {
            mp.stop()
        }
        mp.reset()
        mp.release()
    }

    private fun resetUI() {
        et_answer.setText("")
        tv_currentTime.text = getString(R.string.timer_zero)
        pb_player_timer.progress = 0
        btn_play_question.visibility = View.GONE
        btn_pause_question.visibility = View.GONE
        pb_player_timer.visibility = View.GONE
        pb_load_question.visibility = View.VISIBLE
    }

    private fun setQuestion() {
        currentQuestion = questions[questionIndex]
        hint.text = currentQuestion.answer
        this.supportActionBar?.title = getString(
            R.string.title_question,
            questionIndex + 1,
            numQuestions
        )
        resetQuestionPlayer()
        initQuestionPlayer()
    }

    private fun resetQuestionPlayer() {
        isQuestionPrepared = false
        questionPlayer.reset()
    }

    private fun initQuestionPlayer() {
        questionPlayer.apply {
            setDataSource(currentQuestion.url)
            setOnPreparedListener {
                isQuestionPrepared = true
                pb_load_question.visibility = View.GONE
                tv_duration.text = milliSecondsToTimer(it.duration)
                pb_player_timer.max = it.duration
                showPlayButton()
            }
            setOnCompletionListener {
                tv_currentTime.text = milliSecondsToTimer(it.duration)
                pb_player_timer.progress = it.duration
                showPlayButton()
                if (!isBacksoundMute) {
                    BacksoundManager.play()
                }
            }
            setOnErrorListener { mp, what, extra ->
                Log.d("GameActivity", "WHAT: $what EXTRA: $extra");
                if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    mp.release()
                    questionPlayer = createPlayer()
                    initQuestionPlayer()
                } else {
                    isQuestionPrepared = false
                }
                true
            }
            prepareAsync()
        }
    }

    private fun showPlayButton() {
        btn_pause_question.visibility = View.GONE
        btn_play_question.visibility = View.VISIBLE
        pb_player_timer.visibility = View.VISIBLE
    }

    private fun showPauseButton() {
        btn_play_question.visibility = View.GONE
        btn_pause_question.visibility = View.VISIBLE
        pb_player_timer.visibility = View.VISIBLE
    }

    private fun showMuteButton() {
        btn_unmute.visibility = View.GONE
        btn_mute.visibility = View.VISIBLE
    }

    private fun showUnmuteButton() {
        btn_mute.visibility = View.GONE
        btn_unmute.visibility = View.VISIBLE
    }

    /**
     * Function to convert milliseconds time to Timer Format
     * Hours:Minutes:Seconds
     */
    private fun milliSecondsToTimer(milliseconds: Int): String {
        var finalTimerString = ""
        var secondsString = ""

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60))
        val minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000)
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"

        // return timer string
        return finalTimerString
    }
}