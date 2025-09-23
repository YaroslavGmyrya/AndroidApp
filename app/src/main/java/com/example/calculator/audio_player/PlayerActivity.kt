package com.example.calculator.audio_player

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.calculator.R
import java.io.File
import java.util.Timer
import java.util.TimerTask
import kotlin.random.Random

class PlayerActivity : AppCompatActivity() {

    private var log_tag : String = "MY_LOG_TAG"

    //declaration values

    //Buttons
    private lateinit var prev: ImageButton;
    private lateinit var next: ImageButton;
    private lateinit var playStop: ImageButton;

    //Image
    private lateinit var musicImage: ImageView;

    //Music List
    private var musicIndex: Int = 0;

    //Music player
    private lateinit var player: MediaPlayer;

    //Seek Bar

    private lateinit var seekBar: SeekBar;

    //Timer
    private lateinit var timerTask: TimerTask
    private lateinit var timer: Timer

    //MusicPath and directory
    private lateinit var musicPath: String
    private lateinit var directory: File

    //PathList for music
    private lateinit var musicPathList:Array<File>;

    //ScrollBar for music names
    private lateinit var listMusicName: ScrollView


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

        // Define a requestPermissionLauncher using the RequestPermission contract
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Check if the permission is granted
            if (isGranted) {
                // Show a toast message for permission granted
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
            } else {
                // Show a toast message asking the user to grant the permission
                Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show()
            }
        }

        requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)

        //Update seekbar
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

        //Timer
        timer = Timer();

        //MusicPath
        musicPath = Environment.getExternalStorageDirectory().path + "/testMusic";

        //get directory by path
        directory = File(musicPath);

        //get directory items
        musicPathList = directory.listFiles();

        //ScrollView for music names
        listMusicName = findViewById(R.id.listMusic);

        //buttons
        prev = findViewById(R.id.Prev);
        next = findViewById(R.id.Next);
        playStop = findViewById(R.id.playStop);

        //Image
        musicImage = findViewById(R.id.MusicImage);

        //list of path music
        if (musicPathList != null) {
            musicIndex = Random.nextInt(0, musicPathList.size)
        }

        //Media player
        player = MediaPlayer.create(this, Uri.fromFile(musicPathList?.get(musicIndex)))

        //Seek Bar
        seekBar = findViewById(R.id.seekBar);
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        //TextView in ScrollView
        val ScrollText:TextView = findViewById(R.id.scrollableText)
        musicPathList.forEach {

            //tmp value
            var tmpString : String = it.toString();

            //delete trash from music name
            tmpString = tmpString.replace(musicPath+"/", "");
            tmpString = tmpString.replace("(musmore.com).mp3", "");

            //Add music name to TextView in ScrollBar
            ScrollText.append(tmpString + "\n");
        }

        //start timer
        timer.schedule(timerTask, 0, 10);

        //set action on buttons
        playStop.setOnClickListener({ playStop() })

        prev.setOnClickListener({ prev() })

        next.setOnClickListener({next()})

        //Seek music position, when user UP finger
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

    override fun onPause() {
        super.onPause()
        player.pause();
    }

    //start prev music
    private fun prev(){
        player.release()

        seekBar.setProgress(0);

        musicIndex -= 1;

        if(musicIndex < 0)
            musicIndex = musicPathList.size - 1

        player = MediaPlayer.create(this, Uri.fromFile(musicPathList[musicIndex]))
        player.start()
    }

    //start next music
    private fun next(){
        player.release()

        seekBar.setProgress(0);

        musicIndex += 1;

        if(musicIndex >= musicPathList.size)
            musicIndex = 0

        player = MediaPlayer.create(this, Uri.fromFile(musicPathList[musicIndex]))
        player.start()
    }

    //pause/start
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

}


