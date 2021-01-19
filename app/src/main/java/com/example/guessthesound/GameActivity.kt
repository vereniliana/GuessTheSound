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
import java.lang.Exception

const val GAME_RESULT_KEY = "game_result"

class GameActivity : AppCompatActivity() {

    data class Question(
        val url: String,
        val answer: String
    )

    private val questions: MutableList<Question> = mutableListOf(
        Question(
            url = "https://soundbible.com//mp3/labrador-barking-daniel_simon.mp3",
            answer = "dog"
        ),
        Question(
            url = "https://soundbible.com/mp3/Single%20Cow-SoundBible.com-2051754137.mp3",
            answer = "cow"
        ),
        Question(
            url = "https://soundbible.com/mp3/Frogs-Lisa_Redfern-1150052170.mp3",
            answer = "frog"
        ),
        Question(
            url = "https://soundbible.com/mp3/Cat_Meow_2-Cat_Stevens-2034822903.mp3",
            answer = "cat"
        ),
        Question(
            url = "https://soundbible.com/mp3/Horse%20Neigh-SoundBible.com-1740540960.mp3",
            answer = "horse"
        ),
        Question(
            url = "https://soundbible.com/mp3/Lion%20Roar-SoundBible.com-718441804.mp3",
            answer = "lion"
        ),
        Question(
            url = "https://soundbible.com/mp3/Rooster%20Crow-SoundBible.com-1802551702.mp3",
            answer = "rooster"
        )
    )

    lateinit var currentQuestion: Question
    private var questionIndex = 0
    private val numQuestions = 2

    private val backsoundUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3" // your URL here
    private var isBacksoundMute = false

    private lateinit var mediaPlayer: MediaPlayer
    private var isMediaPrepared: Boolean = false

    private lateinit var questionPlayer: MediaPlayer
    private var isQuestionPrepared: Boolean = false

    private lateinit var buttonSfx: MediaPlayer
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        mediaPlayer = createPlayer()
        questionPlayer = createPlayer()

        buttonSfx = MediaPlayer.create(this, R.raw.button)
        buttonSfx.setVolume(80F,80F)

        // Shuffles the questions and sets the question index to the first question.
        randomizeQuestions()

        mediaPlayer.apply {
            try {
                setDataSource(backsoundUrl)
                isLooping = true
                setOnPreparedListener { mp ->
                    isMediaPrepared = true
                    mp.start()
                }
                prepareAsync()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        btn_play_question.setOnClickListener {
            if (isQuestionPrepared) {
                showPauseButton()
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
                questionPlayer.start()
                updateTime.run()
                updateProgress.run()
            }
        }

        btn_pause_question.setOnClickListener {
            if (questionPlayer.isPlaying) {
                showPlayButton()
                questionPlayer.pause()
                if (!isBacksoundMute && isMediaPrepared) {
                    mediaPlayer.start()
                }
            }
        }

        btn_mute.setOnClickListener {
            isBacksoundMute = true
            showUnmuteButton()
            mediaPlayer.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
        }

        btn_unmute.setOnClickListener {
            isBacksoundMute = false
            showMuteButton()
            if (!isBacksoundMute && isMediaPrepared) {
                mediaPlayer.start()
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
                    )
                    finish()
                }
            } else {
                startActivity(
                    Intent(this, ResultActivity::class.java)
                        .putExtra(GAME_RESULT_KEY, 0)
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
        if (!isBacksoundMute && isMediaPrepared) {
            mediaPlayer.start()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i("MainActivity", "onStop")
        pausePlayer(mediaPlayer)
        pausePlayer(questionPlayer)
    }

    override fun onDestroy() {
        releasePlayer(mediaPlayer)
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

    private fun pausePlayer(mp: MediaPlayer?) {
        mp?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    private fun releasePlayer(mp: MediaPlayer) {
        if (mp.isPlaying) {
            mp.stop()
        }
        mp.release()
    }

    // randomize the questions and set the first question
    private fun randomizeQuestions() {
        questions.shuffle()
        questionIndex = 0
        setQuestion()
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

    // Sets the question and randomizes the answers.  This only changes the data, not the UI.
    // Calling invalidateAll on the FragmentGameBinding updates the data.
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
                Log.d("lalala", "duration: ${it.duration}")
                tv_duration.text = milliSecondsToTimer(it.duration)
                pb_player_timer.max = it.duration
                showPlayButton()
            }
            setOnCompletionListener {
                tv_currentTime.text = milliSecondsToTimer(it.duration)
                pb_player_timer.progress = it.duration
                showPlayButton()
                if (!isBacksoundMute && isMediaPrepared) {
                    mediaPlayer.start()
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