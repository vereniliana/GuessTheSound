package com.example.guessthesound

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

const val GAME_QUESTIONS = "game_questions"

class LoadingActivity : AppCompatActivity() {

    private val questions: ArrayList<Question> = arrayListOf(
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val numQuestions = intent.getIntExtra(NUM_QUESTIONS, 2)

        BacksoundManager.createPlayer(
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            onPreparedListener = { mp ->
                mp.start()
                startActivity(
                    Intent(this, GameActivity::class.java)
                        .putExtra(GAME_QUESTIONS, randomizeQuestions(numQuestions))
                )
                finish()
            }
        )
    }

    private fun randomizeQuestions(numQuestions: Int): ArrayList<Question> {
        questions.shuffle()
        return ArrayList(questions.take(numQuestions))
    }
}