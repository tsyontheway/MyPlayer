package com.example.myplayer

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.controller_layout.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var playerViewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updatePlayerProgress()

        playerViewModel = ViewModelProvider(this).get(PlayerViewModel::class.java).apply {
            progressBarVisibility.observe(this@MainActivity, Observer {
                progressBar.visibility = it
            })

            videoResolution.observe(this@MainActivity, Observer {
                seekBar.max = mediaPlayer.duration
                playerFrame.post {
                    reSizePlayer(it.first, it.second)
                }
            })

            controllerFrameVisibility.observe(this@MainActivity, Observer {
                controllerFrame.visibility = it
            })

            bufferPercent.observe(this@MainActivity, Observer {
                seekBar.secondaryProgress = seekBar.max * it / 100
            })

            playerStatus.observe(this@MainActivity, Observer {
                controllerButton.isClickable = true
                when (it) {
                    PlayerStatus.Paused -> controllerButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    PlayerStatus.Completed -> controllerButton.setImageResource(R.drawable.ic_baseline_replay_24)
                    PlayerStatus.NotReady -> controllerButton.isClickable = false
                    else -> controllerButton.setImageResource(R.drawable.ic_baseline_pause_24)
                }
            })
        }


        lifecycle.addObserver(playerViewModel.mediaPlayer)
        playerFrame.setOnClickListener { playerViewModel.toggleControllerVisibility() }

        controllerButton.setOnClickListener { playerViewModel.togglePlayerStatus() }

        //监听seekBar，并拖动进度条
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    playerViewModel.playerSeekToProgress(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {

            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
                playerViewModel.mediaPlayer.setDisplay(holder)
                playerViewModel.mediaPlayer.setScreenOnWhilePlaying(true)
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }


        })
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI()
            playerViewModel.emmitVideoResolution()
        }
    }


    private fun reSizePlayer(width: Int, height: Int) {
        if (width == 0 || height == 0) return
        surfaceView.layoutParams = FrameLayout.LayoutParams(
            playerFrame.height * width / height,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
    }


    private fun updatePlayerProgress() {
        lifecycleScope.launch {
            while (true) {
                delay(300)
                seekBar.progress = playerViewModel.mediaPlayer.currentPosition
            }
        }
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }


}