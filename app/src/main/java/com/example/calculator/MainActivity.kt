package com.example.calculator

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var hopToPlayer : ImageButton;
    private lateinit var hopToCalc : ImageButton;


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        hopToPlayer = findViewById(R.id.HopToPlayer);
        hopToCalc = findViewById(R.id.hopToCalc);
    }

    override fun onResume() {
        super.onResume()
        hopToPlayer.setOnClickListener({
            // Create an Intent to start the player activity
            val randomIntent = Intent(this@MainActivity, PlayerActivity::class.java)
            // Start the new activity.
            startActivity(randomIntent)
        });

        hopToCalc.setOnClickListener({
            // Create an Intent to start the calculator activity
            val randomIntent = Intent(this@MainActivity, CalculatorActivity::class.java)
            // Start the new activity.
            startActivity(randomIntent)
        });
    }
}