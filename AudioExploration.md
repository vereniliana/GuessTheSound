# Audio Exploration

## MediaPlayer (https://developer.android.com/guide/topics/media/mediaplayer)

An object of this class can fetch, decode, and play both audio and video with minimal setup. Full documentation for MediaPlayer class can be found [here](https://developer.android.com/reference/android/media/MediaPlayer).

### Play Audio

MediaPlayer supports several different media sources such as:

1. Local resources

		var mediaPlayer = MediaPlayer.create(context, R.raw.sound_file_1)
		mediaPlayer.start() // no need to call prepare(); create() does that for you

2. Internal URIs, such as one you might obtain from a Content Resolver

		val myUri: Uri = .... // initialize Uri here
		val mediaPlayer = MediaPlayer().apply {
			setAudioAttributes(
				AudioAttributes.Builder()
					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
					.setUsage(AudioAttributes.USAGE_MEDIA)
					.build()
			)
			setDataSource(applicationContext, myUri)
			prepare()
			start()
		}

3. External URLs (streaming)

		val url =  "http://........"  // your URL here  
		val mediaPlayer =  MediaPlayer().apply { 
			setAudioAttributes(
				AudioAttributes.Builder()  
					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) 
					.setUsage(AudioAttributes.USAGE_MEDIA)  
					.build()  
			) 
			setDataSource(url) 
			prepare() // might take long! (for buffering, etc)
			start()  
		}

	The call to `prepare()` can take a long time to execute, because it might involve fetching and decoding media data. So, it is better to use `prepareAsync()` method. This method starts preparing the media in the background and the `onPrepared()` method of the `MediaPlayer.OnPreparedListener` would be called when the media is done preparing.

		mediaPlayer.apply {
			setOnPreparedListener { mp ->
				mp?.start()  
			}
			prepareAsync()
		}

`setAudioAttribute()` requires API 21 to be called. For API below 21, you can use `setAudioStreamType()`

	MediaPlayer().apply {
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

### Handling Error
Implement `setOnErrorListener` to handle error. `what` is the type of error that has occured, `extra` is an extra code, specific to the error. More explatnation about `OnErrorListener` can be found [here](https://developer.android.com/reference/android/media/MediaPlayer.OnErrorListener).

	setOnErrorListener { mp, what, extra ->
		Log.d("TAG", "WHAT: $what EXTRA: $extra");
		// handle error
		true // Returning false will cause the OnCompletionListener to be called.
	}

### Managing State

MediaPlayer has an internal state that you must always be aware of when writing your code, because certain operations are only valid when then player is in specific states. An IllegalStateException is thrown if a method is called in any invalid state. You can see a list of method valid and invalid state in https://developer.android.com/reference/android/media/MediaPlayer#valid-and-invalid-states .

[![](/mediaplayer_state_diagram.gif)](https://developer.android.com/reference/android/media/MediaPlayer#state-diagram)

Note: MediaPlayer has property `isPlaying` to check if it is in *Started* state, but there is no other method or property to check MediaPlayer's state, so you have to create your own variable if you need it.

### Cleanup
A MediaPlayer can consume valuable system resources, so you should keep it only for as long as you need and call release() when you are done with it to make sure any system resources allocated to it are properly released.

	mediaPlayer?.reset() // see note below
	mediaPlayer?.release()
	mediaPlayer = null

**Note:**
Without `reset()` method, you can still release properly with no error, but you will get warning like this:
> W/MediaPlayer: mediaplayer went away with unhandled events

When you call `reset()`, MediaPlayer is going back to uninitialized state (idle). This way you explicitly discard any unhandled event and the warning will not show up.

### More about MediaPlayer

If you want your media to play in the background even when your application is not onscreen, then you must start a Service and control the MediaPlayer instance from there. (This is usually implemented for music app such as Spotify, more explanation can be found [here](https://developer.android.com/guide/topics/media/mediaplayer#mpandservices))

If you want your media to play continuously within the app but stop when application in the background, you can add LifeCycle to your app. More explanation can be found [here](https://stackoverflow.com/questions/54423895/keep-sound-playing-within-the-app-but-stop-it-when-app-goes-to-the-background/54433734#54433734)