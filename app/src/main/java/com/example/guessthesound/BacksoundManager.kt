package com.example.guessthesound

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build

object BacksoundManager {

    private var mediaPlayer: MediaPlayer? = null
    private var isMediaPrepared: Boolean = false

    fun createPlayer(
        url: String,
        onPreparedListener: ((mp: MediaPlayer) -> Unit)? = null,
        onCompletionListener: ((mp: MediaPlayer) -> Unit)? = null,
        onErrorListener: ((mp: MediaPlayer, what: Int, extra: Int) -> Unit)? = null
    ) {
        if (mediaPlayer == null) {

            mediaPlayer = MediaPlayer().apply {
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

            mediaPlayer?.apply {
                try {
                    setDataSource(url)
                    isLooping = true

                    setOnPreparedListener {
                        isMediaPrepared = true
                        onPreparedListener?.invoke(it)
                    }

                    setOnErrorListener { mp, what, extra ->
                        onErrorListener?.invoke(mp, what, extra)
                        isMediaPrepared = false
                        mp.reset()
                        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                            mp.release()
                            createPlayer(url)
                        }
                        true
                    }

                    setOnCompletionListener {
                        onCompletionListener?.invoke(it)
                    }

                    prepareAsync()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun play() {
        mediaPlayer?.let {
            if (isMediaPrepared && !it.isPlaying) {
                it.start()
            }
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
    }

    fun release() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
            it.release()
            mediaPlayer = null
        }
    }
}