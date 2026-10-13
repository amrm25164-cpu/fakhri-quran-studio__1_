package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val currentUrlOrUri: String? = null,
    val errorMessage: String? = null
)

class AudioPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(AudioPlayerState())
    val state: StateFlow<AudioPlayerState> = _state.asStateFlow()

    fun play(urlOrUri: String) {
        if (_state.value.currentUrlOrUri == urlOrUri && mediaPlayer != null) {
            resume()
            return
        }

        stop()
        _state.value = _state.value.copy(
            isLoading = true,
            currentUrlOrUri = urlOrUri,
            errorMessage = null
        )

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                if (urlOrUri.startsWith("http://") || urlOrUri.startsWith("https://")) {
                    setDataSource(urlOrUri)
                } else {
                    setDataSource(context, Uri.parse(urlOrUri))
                }

                setOnPreparedListener { mp ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isPlaying = true,
                        durationMs = mp.duration
                    )
                    applySpeedAndVolume()
                    mp.start()
                    startProgressTracker()
                }

                setOnCompletionListener {
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        currentPositionMs = 0
                    )
                    stopProgressTracker()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayerManager", "MediaPlayer error: what=$what extra=$extra")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isPlaying = false,
                        errorMessage = "تعذر تشغيل الصوت. يرجى التحقق من الاتصال بالإنترنت أو صحة الملف."
                    )
                    true
                }

                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Exception initializing MediaPlayer", e)
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "خطأ في تهيئة مشغل الصوت: ${e.localizedMessage}"
            )
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _state.value = _state.value.copy(isPlaying = false)
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            it.start()
            _state.value = _state.value.copy(isPlaying = true)
            startProgressTracker()
        }
    }

    fun togglePlayPause() {
        if (_state.value.isPlaying) pause() else resume()
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.let {
            it.seekTo(positionMs)
            _state.value = _state.value.copy(currentPositionMs = positionMs)
        }
    }

    fun setSpeed(speed: Float) {
        _state.value = _state.value.copy(playbackSpeed = speed)
        applySpeedAndVolume()
    }

    fun setVolume(volume: Float) {
        _state.value = _state.value.copy(volume = volume.coerceIn(0f, 1f))
        applySpeedAndVolume()
    }

    private fun applySpeedAndVolume() {
        mediaPlayer?.let { mp ->
            try {
                mp.setVolume(_state.value.volume, _state.value.volume)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mp.isPlaying) {
                    val params = mp.playbackParams
                    params.speed = _state.value.playbackSpeed
                    mp.playbackParams = params
                }
            } catch (e: Exception) {
                Log.w("AudioPlayerManager", "Could not apply speed/volume", e)
            }
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.let { mp ->
                    _state.value = _state.value.copy(currentPositionMs = mp.currentPosition)
                }
                delay(200)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun stop() {
        stopProgressTracker()
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (e: Exception) {
                Log.w("AudioPlayerManager", "Error stopping player", e)
            }
        }
        mediaPlayer = null
        _state.value = _state.value.copy(isPlaying = false, isLoading = false)
    }

    fun release() {
        stop()
    }
}
