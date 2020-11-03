package com.example.myplayer

import android.media.MediaPlayer
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PlayerStatus {
    Playing, Paused, Completed, NotReady
}

class PlayerViewModel : ViewModel() {

    private var controllerShowTime = 0L

    val mediaPlayer = MyMediaPlayer()

    private val _playerStatus = MutableLiveData(PlayerStatus.NotReady)
    val playerStatus: LiveData<PlayerStatus> = _playerStatus

    private val _bufferPercent = MutableLiveData(0)
    val bufferPercent: LiveData<Int> = _bufferPercent

    private val _progressBarVisibility = MutableLiveData(View.VISIBLE)
    val progressBarVisibility: LiveData<Int> = _progressBarVisibility

    private val _controllerFrameVisibility = MutableLiveData(View.VISIBLE)
    val controllerFrameVisibility: LiveData<Int> = _controllerFrameVisibility

    private val _videoResolution = MutableLiveData(Pair(0, 0))
    val videoResolution: LiveData<Pair<Int, Int>> = _videoResolution


    init {
        loadVideo()
    }

    private fun loadVideo() {
        _progressBarVisibility.value = View.VISIBLE
        _playerStatus.value = PlayerStatus.NotReady
        mediaPlayer.apply {
            setDataSource("https://stream7.iqilu.com/10339/upload_transcode/202002/18/20200218093206z8V1JuPlpe.mp4")
            setOnPreparedListener {
                _progressBarVisibility.value = View.INVISIBLE
                it.start()
                _playerStatus.value = PlayerStatus.Playing
            }

            setOnVideoSizeChangedListener { _, width, height ->
                _videoResolution.value = Pair(width, height)
            }

            setOnBufferingUpdateListener { _, percent ->
                _bufferPercent.value = percent
            }
            setOnCompletionListener { _playerStatus.value = PlayerStatus.Completed }

            setOnSeekCompleteListener {
                mediaPlayer.start()
                _progressBarVisibility.value = View.INVISIBLE
            }
            prepareAsync()

        }

    }
    //播放按钮
    fun togglePlayerStatus() {
        when(_playerStatus.value) {
            PlayerStatus.Playing -> {
                mediaPlayer.pause()
                _playerStatus.value = PlayerStatus.Paused
            }
            PlayerStatus.Paused -> {
                mediaPlayer.start()
                _playerStatus.value = PlayerStatus.Playing
            }
            PlayerStatus.Completed -> {
                mediaPlayer.start()
                _playerStatus.value = PlayerStatus.Playing
            }
            else -> return
        }
    }

    fun toggleControllerVisibility() {
        if (_controllerFrameVisibility.value == View.INVISIBLE) {
            _controllerFrameVisibility.value = View.VISIBLE
            controllerShowTime = System.currentTimeMillis()
            viewModelScope.launch {
                delay(3000)
                if (System.currentTimeMillis() - controllerShowTime > 3000) {
                    _controllerFrameVisibility.value = View.INVISIBLE
                }
            }
        } else {
            _controllerFrameVisibility.value = View.INVISIBLE
        }
    }

    fun playerSeekToProgress(progress: Int) {
        _progressBarVisibility.value = View.VISIBLE
        mediaPlayer.seekTo(progress)
    }


    fun emmitVideoResolution() {
        _videoResolution.value = _videoResolution.value
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.release()
    }

}