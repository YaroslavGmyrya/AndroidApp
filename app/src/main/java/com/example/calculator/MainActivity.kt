package com.example.calculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var hopToPlayer : ImageButton;
    private lateinit var hopToCalc : ImageButton;


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