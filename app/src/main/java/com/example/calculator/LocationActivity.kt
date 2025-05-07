package com.example.calculator

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView
import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.gson.GsonBuilder
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PolylineMapObject

import java.io.File

import com.example.calculator.LocationService

class LocationActivity : AppCompatActivity() {

    val value: Int = 0
    val LOG_TAG: String = "LOCATION_ACTIVITY"

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        private const val PERMISSION_REQUEST_FOREGROUND_LOCATION = 100
        private const val PERMISSION_REQUEST_BACKGROUND_LOCATION = 101
    }

    //define variables

    //location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    //textview for info about current location
    private lateinit var tvLat: TextView
    private lateinit var tvLon: TextView
    private lateinit var tvAlt: TextView
    private lateinit var tvAcc: TextView
    private lateinit var tvSpd: TextView
    private lateinit var tvTime: TextView

    //info about signal
    private lateinit var tvSignalType: TextView
    private lateinit var tvSignalLvl: TextView


    //file for info about location
    private lateinit var file: File

    //map
    private lateinit var mapView: MapView

    //route on map
    private lateinit var polylineMapObject: PolylineMapObject
    private val points = mutableListOf<Point>()

    //service for get info about signal quality
    private lateinit var TelephonyManager : TelephonyManager

    //service for location
    private lateinit var locationService : Intent

    //other
    private var isForeground : Boolean = true

    //class location info
    data class info(
        val latitude: Double,
        val longitude: Double,
        val altitude: Double,
        val accuracy: Float,
        val speed: Float,
        val time: Long,
        val net_type: String? = null,
        val signal_lvl: String? = null
    )

    //map for translate code networkType to string
    val networkType = mapOf(
        0 to "undefined",
        1 to "2G (GPRS)",
        2 to "2G (EDGE)",
        3 to "3G (UMTS)",
        4 to "2G (CDMA)",
        5 to "3G (EV-DO)",
        6 to "3G (EV-DO)",
        7 to "2G (1xRTT)",
        8 to "3G (HSDPA)",
        9 to "3G (HSUPA)",
        10 to "3G (HSPA)",
        11 to "2G (iDEN)",
        12 to "3G (EV-DO)",
        13 to "4G (LTE)",
        14 to "3G (eHRPD)",
        15 to "3G (HSPA+)",
        16 to "2G (GSM)",
        17 to "3G (TD-SCDMA)",
        18 to "Wi-Fi",
        19 to "4G+",
        20 to "5G"
    )

    //map for translate code signal lvl to string
    val signal_level = mapOf(
        0 to "No signal",
        1 to "Bad",
        2 to "No good",
        3 to "Good",
        4 to "Great"
    )

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        //set API key from Yandex
        MapKitFactory.setApiKey("8f06c755-f950-4726-a6e0-0a39a2ab4b33")
        MapKitFactory.initialize(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //init map and polyline
        mapView = findViewById(R.id.mapview)
        polylineMapObject = mapView.map.mapObjects.addPolyline(Polyline(emptyList()))

        //init variables
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        tvLat = findViewById(R.id.tv_lat)
        tvLon = findViewById(R.id.tv_lon)
        tvAlt = findViewById(R.id.tv_alt)
        tvAcc = findViewById(R.id.tv_acc)
        tvSpd = findViewById(R.id.tv_spd)
        tvTime = findViewById(R.id.tv_time)
        tvSignalLvl = findViewById(R.id.SignalLvl)
        tvSignalType = findViewById(R.id.tv_signalType)

        file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "info.txt")

        //clear file with prev info about location
        file.writeText("")

        //service for get info about signal
        TelephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        //service for get location
        locationService = Intent(this, LocationService::class.java)


        registerReceiver(locationReceiver, IntentFilter(LocationService.BROADCAST_ACTION))


        createLocationRequest()
        createLocationCallback()

    }

    //if foreground - get location from activity
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart() //start lifecycle mapkit
        mapView.onStart()
        isForeground = true
        stopService(locationService)

    }

    override fun onResume() {
        super.onResume()
        if(isForeground)
            startLocationUpdates()
        else
            stopLocationUpdates()
    }

    //if background - get location from service
    override fun onStop() {
        super.onStop()
        isForeground = false
        startForegroundService(locationService)
    }

    //end lifecycle mapkit and stop fg service
    override fun onDestroy() {
        super.onDestroy()
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        stopService(locationService)
    }

    //check permission and start updates
    private fun startLocationUpdates() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions()
                    return
                }
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                Toast.makeText(applicationContext, "Enable location in settings", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            Log.w(LOG_TAG, "location permission is not allowed")
            requestPermissions()
        }
    }

    //set param for request
    private fun createLocationRequest() {
            locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 0.1f
            maxWaitTime = 0
            isWaitForAccurateLocation = true
        }
    }

    //update location
    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateLocation(location)
                }
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun updateLocation(location: Location) {
        //move camera om first point
        if(points.isEmpty()){
            mapView.map.move(
                CameraPosition(Point(location.latitude, location.longitude), 16f, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        }

        //get network type
        val network_type = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !=
            PackageManager.PERMISSION_GRANTED) {
            networkType.get(0)
        }else
            networkType.get(TelephonyManager.networkType)

        //get signal_lvl
        val signal_lvl = signal_level.get(TelephonyManager.signalStrength?.level)

        // update output info
        tvLat.text = location.latitude.toString()
        tvLon.text = location.longitude.toString()
        tvAlt.text = location.altitude.toString()
        tvAcc.text = location.accuracy.toString()
        tvSpd.text = location.speed.toString()
        tvTime.text = location.time.toString()
        tvSignalLvl.text = signal_lvl
        tvSignalType.text = network_type

        // create object info
        val tmp = info(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            accuracy = location.accuracy,
            speed = location.speed,
            time = location.time,
            net_type = network_type,
            signal_lvl = signal_lvl
        )

        //serializing with Google Gson
        val gson = GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create()

        //add to file
        file.appendText(gson.toJson(tmp))

        //create current_point
        val tmpPoint = Point(location.latitude, location.longitude)

        //add current point to points
        points.add(tmpPoint)

        //get current color
        val currentColor = when(signal_lvl) {
            "Great" -> ContextCompat.getColor(this, R.color.my_green)
            "Good" -> ContextCompat.getColor(this, R.color.my_yellow)
            "No good" -> ContextCompat.getColor(this, R.color.my_orange)
            "Bad" -> ContextCompat.getColor(this, R.color.my_red)
            else -> ContextCompat.getColor(this, R.color.my_black)
        }

        //set color for SignalLvl
        tvSignalLvl.setTextColor(currentColor)

        //if invalid value - set red text
        if (location.speed <= 0.5f || location.accuracy >= 10f) {
            if (location.speed <= 0.5f) {
                tvSpd.setTextColor(ContextCompat.getColor(this@LocationActivity, R.color.my_red))
            }
            if (location.accuracy >= 10f) {
                tvAcc.setTextColor(ContextCompat.getColor(this@LocationActivity, R.color.my_red))
            }
            return
        }

        //if valid value - set default (black) text
        tvSpd.setTextColor(ContextCompat.getColor(this@LocationActivity, R.color.black))
        tvAcc.setTextColor(ContextCompat.getColor(this@LocationActivity, R.color.black))


        //build line
        if (points.size >= 2) {
            //get last 2 point to create line
            val lastTwoPoints = listOf(points[points.size - 2], points[points.size - 1])

            val mapObjects = mapView.map.mapObjects

            //set parameter to new line
            mapObjects.addPolyline(Polyline(lastTwoPoints)).apply {
                strokeWidth = 5f
                setStrokeColor(currentColor)
            }

            // move camera on current point
            mapView.map.move(
                CameraPosition(tmpPoint, mapView.map.cameraPosition.zoom, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 0.5f),
                null
            )
        }
    }

    //receiver from service
    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val location = intent?.getParcelableExtra<Location>("location")
            location?.let {
                Log.w(LOG_TAG, location.toString())
                updateLocation(location)
            }
        }
    }

    //functions for check permission

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,),
                    PERMISSION_REQUEST_FOREGROUND_LOCATION
                )
            }
            else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    PERMISSION_REQUEST_BACKGROUND_LOCATION
                )
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_FOREGROUND_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestPermissions()
                }
            }
            PERMISSION_REQUEST_BACKGROUND_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                }
                }
            }
        }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    }