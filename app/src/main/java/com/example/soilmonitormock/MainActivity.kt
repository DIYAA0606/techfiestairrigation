package com.example.soilmonitormock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.graphics.Color   // ✅ ADDED IMPORT
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val CHANNEL_ID = "irrigation_alerts"

    private var lastMoisture: Int = -1
    private lateinit var tvMoisture: TextView
    private lateinit var tvAlert: TextView
    private lateinit var btnGenerate: Button
    private lateinit var btnHistory: Button
    private lateinit var btnWeather: Button

    private val historyList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnDashboard = findViewById<Button>(R.id.btnDashboard)
        btnDashboard.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        tvAlert = findViewById(R.id.tvAlert)
        tvMoisture = findViewById(R.id.tvMoisture)
        btnGenerate = findViewById(R.id.btnGenerate)
        btnHistory = findViewById(R.id.btnHistory)
        btnWeather = findViewById(R.id.btnWeather)

        createNotificationChannel()
        loadHistory()

        // --- Navigate to Weather Screen ---
        btnWeather.setOnClickListener {
            val intent = Intent(this, WeatherActivity::class.java)
            intent.putExtra("moisture", lastMoisture)
            startActivity(intent)
        }

        // --- Generate Moisture ---
        btnGenerate.setOnClickListener {
            val moisture = Random.nextInt(0, 100)
            lastMoisture = moisture

            // Save last moisture + time
            val prefs = getSharedPreferences("soil_prefs", MODE_PRIVATE)
            prefs.edit()
                .putInt("last_moisture", moisture)
                .putLong("last_moisture_time", System.currentTimeMillis())
                .apply()

            tvMoisture.text = "Moisture: $moisture%"
            historyList.add(moisture)
            saveHistory()

            // Existing UI + notification logic
            updateMoistureStatus(moisture)
            sendMoistureBasedNotification(moisture)
        }

        // --- History Screen ---
        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putIntegerArrayListExtra("history", ArrayList(historyList))
            startActivity(intent)
        }
    }

    // ---------------------------------------------------------
    //   UPDATE UI BASED ON MOISTURE LEVEL
    // ---------------------------------------------------------
    private fun updateMoistureStatus(moisture: Int) {
        when {
            moisture < 30 -> {
                tvAlert.text = "Low Moisture – Water Needed."
                tvAlert.setTextColor(getColor(android.R.color.holo_red_dark))
            }
            moisture in 30..70 -> {
                tvAlert.text = "Optimal Moisture – Soil is Healthy."
                tvAlert.setTextColor(getColor(android.R.color.holo_green_dark))
            }
            moisture > 70 -> {
                tvAlert.text = "High Moisture – No Watering Required."
                tvAlert.setTextColor(getColor(android.R.color.holo_blue_dark))
            }
        }
    }

    // ---------------------------------------------------------
    //   NOTIFICATION SYSTEM
    // ---------------------------------------------------------
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Irrigation Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendMoistureBasedNotification(moisture: Int) {
        when {
            moisture < 25 -> {
                sendNotification(
                    "Low Moisture",
                    "Soil moisture is critically low. Irrigation required immediately."
                )
            }
            moisture > 85 -> {
                sendNotification(
                    "High Moisture",
                    "Soil moisture is too high. Stop irrigation to prevent crop damage."
                )
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    // ---------------------------------------------------------
    //   LOCAL STORAGE FOR HISTORY (SharedPreferences)
    // ---------------------------------------------------------
    private fun saveHistory() {
        val prefs = getSharedPreferences("soil_prefs", MODE_PRIVATE)
        prefs.edit().putString("history_list", historyList.joinToString(",")).apply()
    }

    private fun loadHistory() {
        val prefs = getSharedPreferences("soil_prefs", MODE_PRIVATE)
        val savedData = prefs.getString("history_list", "")

        if (!savedData.isNullOrEmpty()) {
            historyList.clear()
            historyList.addAll(savedData.split(",").map { it.toInt() })
        }
    }

    // ---------------------------------------------------------
    //   🔥 EXTRA SMART DECISION BLOCK (HACKATHON LOGIC)
    // ---------------------------------------------------------
    private fun moistureDecision(moisture: Int): Pair<String, Int> {
        return when {
            moisture < 30 ->
                Pair("Low Moisture – Water Needed.", Color.RED)

            moisture in 30..60 ->
                Pair("Moderate Moisture – Monitor.", Color.parseColor("#FFA000"))

            else ->
                Pair("Good Moisture – No Action Needed.", Color.parseColor("#2E7D32"))
        }
    }
}
