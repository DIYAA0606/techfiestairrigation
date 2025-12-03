Smart Irrigation & Soil Monitoring App

A Kotlin-based Android application that simulates soil moisture readings, fetches real-time weather data using the OpenWeather API, provides irrigation recommendations through rule-based logic, tracks moisture history, and is structured for future integration with IoT hardware (Raspberry Pi and sensors).

Overview

This project is an early MVP (Minimum Viable Product) for a smart irrigation system.
It enables users to monitor:

Temperature

Soil Moisture

Weather Conditions

Moisture History

Irrigation Recommendations

The current version uses simulated soil moisture values and real weather data.
The architecture allows smooth integration with Raspberry Pi-based sensor hardware later.

Features
Soil Moisture Simulation

Generates random moisture percentage values to mimic real sensor input.

Real-Time Weather Tracking

Uses OpenWeatherMap API to fetch:

Temperature

Humidity

Weather condition

Location using GPS

Irrigation Recommendation System

Simple AI-style rule engine:

Moisture < 30% → Water immediately

Moisture > 70% → No watering needed

Rain expected → Pause irrigation

Low humidity or high temperature increases irrigation requirement

Moisture History

Stores previous moisture readings using SharedPreferences.

Dashboard Screen

Shows a summarized view:

Temperature

Moisture

Weather condition

Humidity

Quick navigation buttons

Clear Navigation Flow

Main screen → Weather screen → Dashboard → History screen

Raspberry Pi Ready

The codebase supports future sensor connectivity through REST API, MQTT, or local networking.

Tech Stack
Technology	Purpose
Kotlin	Application logic
Android Studio	Development environment
Retrofit2	Network communication
OpenWeatherMap API	Weather data
Google Play Services (FusedLocationProviderClient)	Location services
SharedPreferences	Local data storage
Material Design	User interface styling
Project Structure
app/
 ├── manifests/
 ├── java/
 │    └── com.example.soilmonitormock
 │          ├── MainActivity.kt
 │          ├── WeatherActivity.kt
 │          ├── DashboardActivity.kt
 │          ├── HistoryActivity.kt
 │          ├── RetrofitInstance.kt
 │          ├── WeatherApiService.kt
 │          ├── WeatherResponse.kt
 │
 ├── res/
 │   ├── layout/
 │   │     ├── activity_main.xml
 │   │     ├── activity_weather.xml
 │   │     ├── activity_dashboard.xml
 │   │     ├── activity_history.xml
 │   ├── drawable/
 │   ├── values/
 │
 ├── build.gradle.kts

Requirements
Android Studio (Hedgehog or newer)
OpenWeatherMap API Key

Get an API key at:
https://openweathermap.org/api

Insert your key in:

private val apiKey = "YOUR_API_KEY"

How to Run the Project

Clone the repository

git clone https://github.com/yourusername/yourrepository.git


Open the folder in Android Studio.

Insert your OpenWeather API key.

Run the app on an emulator or physical device.

Allow location permission when prompted.

Raspberry Pi Integration Roadmap
Phase 1: Sensor Input

Connect the app to Raspberry Pi through:

REST API endpoint

MQTT topic (soil/moisture)

Phase 2: Live Data Dashboard

Add real-time updates from sensors such as:

Soil moisture probes

DHT11/DHT22 temperature-humidity sensors

Phase 3: Automatic Irrigation Control

Trigger water pumps via:

Relay module

Raspberry Pi GPIO pins

Phase 4: Cloud Synchronization

Possible integrations:

Firebase

AWS IoT

ThingsBoard

Node-RED

Contributors
Name	Role
Your Name	Android Developer
Teammate 1	IoT / Hardware
Teammate 2	Backend API
Teammate 3	UI/UX
License

This project is released under the MIT License. You may modify or extend the code for personal or commercial use.
