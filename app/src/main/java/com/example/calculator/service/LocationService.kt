package com.example.calculator.service
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.calculator.R
import com.google.android.gms.location.*

class LocationService : Service() {
    private val LOG_TAG = "LocationService"
    private val CHANNEL_ID = "locationChannel"
    private val NOTIFICATION_ID = 101

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        startForegroundService()
        createLocationRequest()
        createLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Tracker",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS Tracker")
            .setContentText("Track your location")
            .setSmallIcon(R.drawable.location)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .apply {
                setChannelId(CHANNEL_ID)
                setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            }
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createLocationRequest() {
        locationRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000
            ).apply {
                setMinUpdateIntervalMillis(500)
                setWaitForAccurateLocation(false)
            }.build()
        } else {
            LocationRequest.create().apply {
                interval = 1000
                fastestInterval = 500
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                smallestDisplacement = 0f
                isWaitForAccurateLocation = false
            }
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.w(LOG_TAG, "SEND LOCATION TO ACTIVITY")
                    sendLocationToActivity(location)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(LOG_TAG, "Permission denied! Stop service.")
            stopSelf()
        }
    }


    private fun sendLocationToActivity(location: Location) {
        val intent = Intent(BROADCAST_ACTION).apply {
            putExtra("location", location)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            setPackage(packageName)

        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
    }

    companion object {
        const val BROADCAST_ACTION = "ACTION"
    }
}