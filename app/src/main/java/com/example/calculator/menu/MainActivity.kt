package com.example.calculator.menu

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.calculator.audio_player.PlayerActivity
import com.example.calculator.R
import com.example.calculator.calculator.CalculatorActivity
import com.example.calculator.location.LocationActivity


class MainActivity : AppCompatActivity() {
    //define icon for activities
    private lateinit var hopToPlayer : ImageButton;
    private lateinit var hopToCalc : ImageButton;
    private lateinit var hopToLocation: ImageButton;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //icon init
        hopToPlayer = findViewById(R.id.HopToPlayer);
        hopToCalc = findViewById(R.id.hopToCalc);
        hopToLocation = findViewById((R.id.HopToLocation))
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

        hopToLocation.setOnClickListener({
            // Create an Intent to start the calculator activity
            val randomIntent = Intent(this@MainActivity, LocationActivity::class.java)
            // Start the new activity.
            startActivity(randomIntent)
        });
    }
}