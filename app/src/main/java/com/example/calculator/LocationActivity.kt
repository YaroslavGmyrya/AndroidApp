package com.example.calculator

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView
import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import android.os.Environment
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.gson.GsonBuilder
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PolylineMapObject

import java.io.File

class LocationActivity : AppCompatActivity() {

    val value: Int = 0
    val LOG_TAG: String = "LOCATION_ACTIVITY"

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
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
    private lateinit var SignalType: TextView
    private lateinit var SignalLvl: TextView


    //file for info about location
    private lateinit var file: File

    //map
    private lateinit var mapView: MapView

    //route on map
    private lateinit var polylineMapObject: PolylineMapObject
    private val points = mutableListOf<Point>()

    //service for get info about signal quality
    private lateinit var TelephonyManager : TelephonyManager

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
        0 to "no signal",
        1 to "bad",
        2 to "no good",
        3 to "good",
        4 to "great"
    )

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

        //move camera on start pos
        mapView.map.move(
            CameraPosition(Point(52.9885448, 78.6487029), 12f, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )

        //check permission on write in storage
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show()
            }
        }

        //request permission
        requestPermissionLauncher.launch(WRITE_EXTERNAL_STORAGE)

        //check permission on Phone state
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //request permission
            requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }

        //init variables
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        tvLat = findViewById(R.id.tv_lat)
        tvLon = findViewById(R.id.tv_lon)
        tvAlt = findViewById(R.id.tv_alt)
        tvAcc = findViewById(R.id.tv_acc)
        tvSpd = findViewById(R.id.tv_spd)
        tvTime = findViewById(R.id.tv_time)
        SignalLvl = findViewById(R.id.SignalLvl)
        SignalType = findViewById(R.id.SignalType)

        file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "info.txt")

        //clear file with prev info about location
        file.writeText("")

        createLocationRequest()
        createLocationCallback()

        //service for get info about signal
        TelephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        Log.w(LOG_TAG, TelephonyManager.networkType.toString())
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart() //start lifecycle mapkit
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop() //end lifecycle mapkit
        super.onStop()
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

    @SuppressLint("SetTextI18n")
    private fun updateLocation(location: Location) {
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
        SignalLvl.text = signal_lvl
        SignalType.text = network_type

        if(location.speed <= 0.5f || location.accuracy >= 10)
            return

        //create current_point
        val tmpPoint = Point(location.latitude, location.longitude)

        //add current point to points
        points.add(tmpPoint)

        if (points.size >= 2) {
            //get last 2 point to create line
            val lastTwoPoints = listOf(points[points.size - 2], points[points.size - 1])

            val mapObjects = mapView.map.mapObjects

            //get current color
            val currentColor = when(signal_lvl) {
                "great" -> ContextCompat.getColor(this, R.color.my_green)
                "good" -> ContextCompat.getColor(this, R.color.my_yellow)
                "no good" -> ContextCompat.getColor(this, R.color.my_orange)
                "bad" -> ContextCompat.getColor(this, R.color.my_red)
                else -> ContextCompat.getColor(this, R.color.my_black)
            }

            //set parameter to new line
            mapObjects.addPolyline(Polyline(lastTwoPoints)).apply {
                strokeWidth = 1f
                setStrokeColor(currentColor)
                gradientLength = 1f
                outlineColor = ContextCompat.getColor(this@LocationActivity, R.color.black)
                outlineWidth = 2f
            }

            // move camera on current point
            mapView.map.move(
                CameraPosition(tmpPoint, mapView.map.cameraPosition.zoom, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 0.5f),
                null
            )
        }

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
    }

    //functions for check permission

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        Log.w(LOG_TAG, "requestPermissions()")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Permission granted", Toast.LENGTH_SHORT).show()
                startLocationUpdates()
            } else {
                Toast.makeText(applicationContext, "Denied by user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}