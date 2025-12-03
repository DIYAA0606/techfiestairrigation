package com.example.soilmonitormock

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val listView = findViewById<ListView>(R.id.listViewHistory)

        val history = intent.getIntegerArrayListExtra("history") ?: arrayListOf()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            history.map { "Moisture: $it%" }
        )

        listView.adapter = adapter
    }
}
