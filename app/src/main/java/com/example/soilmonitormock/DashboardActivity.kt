package com.example.soilmonitormock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // UI elements
        val tvMoisture = findViewById<TextView>(R.id.tvDashMoisture)
        val tvWeatherTemp = findViewById<TextView>(R.id.tvDashTemp)
        val tvWeatherCond = findViewById<TextView>(R.id.tvDashCondition)
        val tvWeatherHumidity = findViewById<TextView>(R.id.tvDashHumidity)

        val btnGoMain = findViewById<Button>(R.id.btnGoMain)
        val btnGoWeather = findViewById<Button>(R.id.btnGoWeather)
        val btnGoHistory = findViewById<Button>(R.id.btnGoHistory)

        // Load saved values
        val prefs = getSharedPreferences("soil_prefs", MODE_PRIVATE)

        val lastMoisture = prefs.getInt("last_moisture", -1)
        val lastTemp = prefs.getFloat("last_temp", -1f)
        val lastHumidity = prefs.getInt("last_humidity", -1)
        val lastCondition = prefs.getString("last_condition", "--")
        val lastLocation = prefs.getString("last_location", "--")

        // Update UI
        tvMoisture.text = if (lastMoisture == -1) {
            "Moisture: --%"
        } else {
            "Moisture: $lastMoisture%"
        }

        if (lastTemp != -1f) {
            tvWeatherTemp.text = "Temperature: $lastTemp°C"
        }

        if (lastHumidity != -1) {
            tvWeatherHumidity.text = "Humidity: $lastHumidity%"
        }

        tvWeatherCond.text = "Condition: $lastCondition ($lastLocation)"

        // Buttons navigation
        btnGoMain.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        btnGoWeather.setOnClickListener {
            startActivity(Intent(this, WeatherActivity::class.java))
        }

        btnGoHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }
}
