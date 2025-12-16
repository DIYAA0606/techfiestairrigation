package com.example.soilmonitormock

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val CHANNEL_ID = "irrigation_alerts"
    private val NOTIFICATION_PERMISSION_CODE = 101

    private var lastMoisture: Int = -1

    private lateinit var tvMoisture: TextView
    private lateinit var tvAlert: TextView
    private lateinit var btnGenerate: Button
    private lateinit var btnHistory: Button
    private lateinit var btnWeather: Button
    private lateinit var btnDashboard: Button

    private val historyList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI bindings
        tvMoisture = findViewById(R.id.tvMoisture)
        tvAlert = findViewById(R.id.tvAlert)
        btnGenerate = findViewById(R.id.btnGenerate)
        btnHistory = findViewById(R.id.btnHistory)
        btnWeather = findViewById(R.id.btnWeather)
        btnDashboard = findViewById(R.id.btnDashboard)

        createNotificationChannel()
        requestNotificationPermissionIfNeeded()
        loadHistory()

        // Dashboard
        btnDashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        // Weather
        btnWeather.setOnClickListener {
            val intent = Intent(this, WeatherActivity::class.java)
            intent.putExtra("moisture", lastMoisture)
            startActivity(intent)
        }

        // Generate Moisture (mock for now)
        btnGenerate.setOnClickListener {
            val moisture = Random.nextInt(0, 100)
            lastMoisture = moisture

            // Save last moisture
            val prefs = getSharedPreferences("soil_prefs", MODE_PRIVATE)
            prefs.edit()
                .putInt("last_moisture", moisture)
                .putLong("last_moisture_time", System.currentTimeMillis())
                .apply()

            tvMoisture.text = "Moisture: $moisture%"
            historyList.add(moisture)
            saveHistory()

            updateMoistureStatus(moisture)
            sendMoistureBasedNotification(moisture)
        }

        // History
        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putIntegerArrayListExtra("history", ArrayList(historyList))
            startActivity(intent)
        }
    }

    // -------------------------------------------------
    // Moisture Status UI
    // -------------------------------------------------
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
            else -> {
                tvAlert.text = "High Moisture – No Watering Required."
                tvAlert.setTextColor(getColor(android.R.color.holo_blue_dark))
            }
        }
    }

    // -------------------------------------------------
    // Notification System
    // -------------------------------------------------
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

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    private fun sendMoistureBasedNotification(moisture: Int) {
        when {
            moisture < 25 -> {
                sendNotification(
                    "Low Moisture",
                    "Soil moisture is critically low. Irrigation required."
                )
            }
            moisture > 85 -> {
                sendNotification(
                    "High Moisture",
                    "Soil moisture is too high. Stop irrigation."
                )
            }
        }
    }

    private fun sendNotification(title: String, message: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    // -------------------------------------------------
    // History Storage
    // -------------------------------------------------
    private fun saveHistory() {
        val prefs = getSharedPreferences("soil_prefs", MODE_PRIVATE)
        prefs.edit().putString("history_list", historyList.joinToString(",")).apply()
    }

    private fun loadHistory() {
        val prefs = getSharedPreferences("soil_prefs", MODE_PRIVATE)
        val saved = prefs.getString("history_list", "")
        if (!saved.isNullOrEmpty()) {
            historyList.clear()
            historyList.addAll(saved.split(",").map { it.toInt() })
        }
    }
}
