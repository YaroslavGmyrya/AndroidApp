package com.example.calculator

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Timer
import java.util.TimerTask
import kotlin.random.Random


class PlayerActivity : AppCompatActivity() {
    //declaration values

    //Buttons
    private lateinit var prev: ImageButton;
    private lateinit var next: ImageButton;
    private lateinit var playStop: ImageButton;

    //Image
    private lateinit var musicImage: ImageView;

    //Music List
    private var musicList: IntArray = intArrayOf();
    private var musicIndex: Int = 0;

    //Music player
    private lateinit var player: MediaPlayer;

    //Seek Bar
    private lateinit var seekBar: SeekBar;

    //Timer
    private lateinit var timerTask: TimerTask
    private lateinit var timer: Timer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        timerTask = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    seekBar.setProgress(player.currentPosition * seekBar.max / player.duration)

                    if (seekBar.progress >= 99)
                        next();
                }
            }
        }

        //initialization
        timer = Timer();

        //buttons
        prev = findViewById(R.id.Prev);
        next = findViewById(R.id.Next);
        playStop = findViewById(R.id.playStop);

        //Image
        musicImage = findViewById(R.id.MusicImage);

        //music list
        musicList = intArrayOf(R.raw.acdc, R.raw.nirvana)
        musicIndex = Random.nextInt(0, musicList.size)

        //Media player
        player = MediaPlayer.create(this, musicList[musicIndex])

        //Seek Bar
        seekBar = findViewById(R.id.seekBar);
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        timer.schedule(timerTask, 0, 10);

        playStop.setOnClickListener({ playStop() })

        prev.setOnClickListener({ prev() })

        next.setOnClickListener({next()})

        seekBar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    player.seekTo((player.duration * seekBar.progress) / seekBar.max)
                    true
                }
                else -> false
            }
        }


    }

    private fun prev(){
        player.release()

        seekBar.setProgress(0);

        musicIndex -= 1;

        if(musicIndex < 0)
            musicIndex = musicList.size - 1

        player = MediaPlayer.create(this, musicList[musicIndex])
        player.start()
    }

    private fun next(){
        player.release()

        seekBar.setProgress(0);

        musicIndex += 1;

        if(musicIndex >= musicList.size)
            musicIndex = 0

        player = MediaPlayer.create(this, musicList[musicIndex])
        player.start()
    }

    private fun playStop(){
        if(player.isPlaying){
            playStop.setImageResource(android.R.drawable.ic_media_play);
            player.pause();
        }

        else {
            playStop.setImageResource(android.R.drawable.ic_media_pause);
            player.start();
        }
    }

    override fun onPause() {
        super.onPause()
        player.pause();
    }

}


