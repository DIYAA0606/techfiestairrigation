package com.example.soilmonitormock

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherActivity : AppCompatActivity() {

    private val apiKey = "4415e0577b67054c93de0183d3e7307b"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val LOCATION_REQUEST_CODE = 1001
    private val REFRESH_INTERVAL = 5 * 60 * 1000L // 5 minutes

    private var lastLat = 0.0
    private var lastLon = 0.0

    private var moisture = -1

    private val handler = Handler(Looper.getMainLooper())
    private val autoRefreshRunnable = object : Runnable {
        override fun run() {
            getLocationAndFetchWeather(moisture)
            handler.postDelayed(this, REFRESH_INTERVAL)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        moisture = intent.getIntExtra("moisture", -1)
        autoRefreshWeather(moisture)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Request permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        } else {
            getLocationAndFetchWeather(moisture)
        }

        // Build location callback for auto-detecting changes
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val newLoc = result.lastLocation
                if (newLoc != null) {
                    if (newLoc.latitude != lastLat || newLoc.longitude != lastLon) {
                        lastLat = newLoc.latitude
                        lastLon = newLoc.longitude
                        fetchWeather(lastLat, lastLon, moisture)
                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            60000 // 1 min
        ).build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun getLocationAndFetchWeather(moisture: Int) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
            val lat = loc?.latitude ?: 18.5204
            val lon = loc?.longitude ?: 73.8567

            lastLat = lat
            lastLon = lon

            fetchWeather(lat, lon, moisture)
        }
    }

    private fun fetchWeather(lat: Double, lon: Double, moisture: Int) {

        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        val tvTemp = findViewById<TextView>(R.id.tvTemp)
        val tvHumidity = findViewById<TextView>(R.id.tvHumidity)
        val tvCondition = findViewById<TextView>(R.id.tvCondition)
        val tvAdvice = findViewById<TextView>(R.id.tvAdvice)

        RetrofitInstance.api.getWeatherByCoords(lat, lon, apiKey)
            .enqueue(object : Callback<WeatherResponse> {

                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val weather = response.body()

                        val temp = weather?.main?.temp ?: 0f
                        val humidity = weather?.main?.humidity ?: 0
                        val condition = weather?.weather?.get(0)?.description ?: ""
                        // Save latest weather to prefs
                        val prefs = getSharedPreferences("soil_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putFloat("last_temp", temp)
                            .putInt("last_humidity", humidity)
                            .putString("last_condition", condition)
                            .putString("last_location", weather?.name ?: "--")
                            .apply()

                        tvLocation.text = "Location: ${weather?.name}"
                        tvTemp.text = "Temperature: ${temp}°C"
                        tvHumidity.text = "Humidity: ${humidity}%"
                        tvCondition.text = "Condition: $condition"

                        val advice = when {
                            condition.contains("rain", true) ->
                                "Rain expected – Stop irrigation for 24 hours."
                            moisture < 30 ->
                                "Low moisture – Water now."
                            moisture > 70 ->
                                "High moisture – No watering needed."
                            humidity < 40 ->
                                "Dry air – Increase irrigation."
                            temp > 32 ->
                                "Hot weather – Increase irrigation."
                            else ->
                                "Normal conditions – Moderate irrigation."
                        }

                        tvAdvice.text = advice

                    } else {
                        tvCondition.text = "Failed to load weather."
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    tvCondition.text = "Error: ${t.message}"
                }
            })
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        handler.post(autoRefreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        handler.removeCallbacks(autoRefreshRunnable)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getLocationAndFetchWeather(moisture)
        }
    }
    private fun autoRefreshWeather(moisture: Int) {
        val handler = android.os.Handler(mainLooper)
        val refreshTask = object : Runnable {
            override fun run() {
                getLocationAndFetchWeather(moisture)
                handler.postDelayed(this, 5 * 60 * 1000) // refresh every 5 minutes
            }
        }
        handler.post(refreshTask)
    }
}
