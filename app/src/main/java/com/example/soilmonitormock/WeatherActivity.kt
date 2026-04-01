package com.example.soilmonitormock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherActivity : AppCompatActivity() {

    private val apiKey = "YOUR_OPENWEATHER_API_KEY"
    private val LOCATION_REQUEST_CODE = 1001

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var tvLocation: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvCondition: TextView
    private lateinit var tvAdvice: TextView

    private var moisture: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        moisture = intent.getIntExtra("moisture", -1)

        tvLocation = findViewById(R.id.tvLocation)
        tvTemp = findViewById(R.id.tvTemp)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvCondition = findViewById(R.id.tvCondition)
        tvAdvice = findViewById(R.id.tvAdvice)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchLocation()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                tvCondition.text = "Location permission required"
                tvAdvice.text = "Please enable location permission from settings"
                openAppSettings()
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_REQUEST_CODE
                )
            }
        }
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
            fetchLocation()
        } else {
            tvCondition.text = "Location permission denied"
            tvAdvice.text = "Enable location to view weather details"
            openAppSettings()
        }
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                fetchWeather(location.latitude, location.longitude)
            } else {
                tvCondition.text = "Unable to get location"
            }
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        RetrofitInstance.weatherApi.getWeatherByCoords(
            lat,
            lon,
            apiKey
        ).enqueue(object : Callback<WeatherResponse> {

            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val weather = response.body()!!

                    tvLocation.text = "Location: ${weather.name}"
                    tvTemp.text = "Temperature: ${weather.main.temp}°C"
                    tvHumidity.text = "Humidity: ${weather.main.humidity}%"
                    tvCondition.text = "Condition: ${weather.weather[0].description}"

                    tvAdvice.text = getIrrigationAdvice(
                        weather.main.temp,
                        weather.main.humidity,
                        moisture,
                        weather.weather[0].description
                    )
                } else {
                    tvCondition.text = "Weather data unavailable"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                tvCondition.text = "Network error"
            }
        })
    }

    private fun getIrrigationAdvice(
        temp: Float,
        humidity: Int,
        moisture: Int,
        condition: String
    ): String {
        return when {
            condition.contains("rain", true) ->
                "Rain expected. Irrigation not required."
            moisture < 30 ->
                "Low soil moisture. Watering recommended."
            moisture > 70 ->
                "Soil moisture sufficient. No irrigation needed."
            temp > 32 ->
                "High temperature. Monitor irrigation."
            else ->
                "Conditions normal."
        }
    }
}

