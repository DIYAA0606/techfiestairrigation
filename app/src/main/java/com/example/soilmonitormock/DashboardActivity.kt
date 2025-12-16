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

        // ---------------- UI ELEMENTS (MATCHING XML IDs) ----------------
        val tvTemp = findViewById<TextView>(R.id.tvDashTemp)
        val tvMoisture = findViewById<TextView>(R.id.tvDashMoisture)
        val tvHumidity = findViewById<TextView>(R.id.tvDashHumidity)
        val tvCondition = findViewById<TextView>(R.id.tvDashCondition)
        val tvFinalDecision = findViewById<TextView>(R.id.tvFinalDecision)
        val switchAuto = findViewById<android.widget.Switch>(R.id.switchAutoIrrigation)


        val btnGoMain = findViewById<Button>(R.id.btnGoMain)
        val btnGoWeather = findViewById<Button>(R.id.btnGoWeather)
        val btnGoHistory = findViewById<Button>(R.id.btnGoHistory)

        // ---------------- READ SAVED DATA ----------------
        val prefs = getSharedPreferences("soil_prefs", MODE_PRIVATE)

        val moisture = prefs.getInt("last_moisture", -1)
        val temperature = prefs.getFloat("last_temp", 0f)
        val humidity = prefs.getInt("last_humidity", 0)
        val condition = prefs.getString("last_condition", "unknown") ?: "unknown"

        // ---------------- DISPLAY DATA ----------------
        tvTemp.text = "Temperature: $temperature°C"
        tvMoisture.text = "Moisture: $moisture%"
        tvHumidity.text = "Humidity: $humidity%"
        tvCondition.text = "Condition: $condition"

        // ---------------- FINAL SMART DECISION ----------------
        val decision = finalIrrigationDecision(moisture, condition)
        tvFinalDecision.text = decision

        val prediction = predictTomorrow(moisture, temperature, condition)
        tvFinalDecision.append("\n\nPrediction: $prediction")


        switchAuto.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                when {
                    condition.contains("rain", ignoreCase = true) -> {
                        tvFinalDecision.text = "🌧️ Rain detected – Auto irrigation OFF"
                    }

                    moisture < 30 -> {
                        tvFinalDecision.text = "🚿 Auto irrigation STARTED"
                    }

                    else -> {
                        tvFinalDecision.text = "🌱 Soil moisture sufficient – No irrigation"
                    }
                }
            } else {
                tvFinalDecision.text = "Auto irrigation disabled"
            }
        }


        // ---------------- BUTTON NAVIGATION ----------------
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

    // ---------------- SMART IRRIGATION LOGIC ----------------
    private fun finalIrrigationDecision(
        moisture: Int,
        condition: String
    ): String {
        return when {
            condition.contains("rain", ignoreCase = true) ->
                "Rain Expected 🌧️ – Do Not Irrigate"

            moisture < 30 ->
                "Low Moisture 🚨 – Irrigate Now"

            else ->
                "Conditions Optimal 🌱 – No Irrigation Needed"
        }
    }

    private fun predictTomorrow(
        moisture: Int,
        temperature: Float,
        condition: String
    ): String {
        return when {
            condition.contains("rain", true) ->
                "🌧️ Rain expected tomorrow – Soil moisture will remain high"

            temperature > 32 && moisture < 40 ->
                "☀️ Hot weather ahead – High chance of soil drying"

            moisture > 70 ->
                "💧 Soil already wet – No irrigation needed tomorrow"

            else ->
                "🌱 Conditions stable – Normal irrigation tomorrow"
        }
    }

}
