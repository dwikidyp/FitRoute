package com.fitroute.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.fitroute.R
import com.fitroute.domain.usecase.CalorieCalculationUseCase
import com.fitroute.domain.usecase.MetValue
import com.fitroute.util.ActivityDetector
import com.fitroute.util.ActivityType
import com.fitroute.util.SensorFusionManager

class TrackingService : Service() {

    // Client GPS dari Google Play Services
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var activityDetector: ActivityDetector

    // Daftar titik koordinat rute
    private val routePoints = mutableListOf<LatLng>()

    private lateinit var sensorFusion: SensorFusionManager
    private var currentElevation = 0.0
    private var totalElevationGain = 0.0
    private var lastElevation = 0.0
    private val calorieUseCase = CalorieCalculationUseCase()
    private var weightKg = 70.0
    private var startTimeMs = 0L
    private var totalDistanceKm = 0.0
    private var lastLocation: Location? = null

    companion object {
        const val CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_ID = 1

        // Untuk komunikasi dengan Fragment
        var isRunning = false
        var latestLocation: LatLng? = null
        var latestElevation = 0.0
        var totalElevationGain = 0.0
        var currentActivity = ActivityType.UNKNOWN
        var totalCalories = 0.0
        var totalDistanceKm = 0.0
        var durationSeconds = 0L
    }

    override fun onCreate() {
        super.onCreate()

        // Tampilkan notifikasi agar service tidak dibunuh Android
        startForeground(NOTIFICATION_ID, buildTrackingNotification())
        startTimeMs = System.currentTimeMillis()

        // Inisialisasi GPS client
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        sensorFusion = SensorFusionManager(this)
        sensorFusion.onElevationUpdated = { elevation ->
            currentElevation = elevation
            latestElevation = elevation

            // Hitung total elevasi naik
            if (lastElevation > 0 && elevation > lastElevation) {
                totalElevationGain += (elevation - lastElevation)
                Companion.totalElevationGain = totalElevationGain
            }
            lastElevation = elevation
        }
        sensorFusion.start()

        // Activity detector
        activityDetector = ActivityDetector(this)
        activityDetector.onActivityDetected = { type ->
            currentActivity = type
        }
        activityDetector.start()

        // Mulai terima update lokasi
        startLocationUpdates()

        isRunning = true
    }

    private fun startLocationUpdates() {
        // Request lokasi setiap 2 detik, akurasi tinggi
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        )
            .setMinUpdateDistanceMeters(3f)
            .build()

        try {
            fusedClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // Callback dipanggil setiap ada update lokasi baru
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                val point = LatLng(loc.latitude, loc.longitude)

                // Simpan titik ke daftar rute
                routePoints.add(point)

                // Simpan lokasi terbaru untuk diakses Fragment
                latestLocation = point

                // Hitung jarak antar titik
                lastLocation?.let { prev ->
                    val distance = FloatArray(1)
                    Location.distanceBetween(
                        prev.latitude, prev.longitude,
                        loc.latitude, loc.longitude,
                        distance
                    )
                    totalDistanceKm += distance[0] / 1000.0
                    Companion.totalDistanceKm = totalDistanceKm
                }
                lastLocation = loc

                // Hitung durasi
                val durationHours = (System.currentTimeMillis() - startTimeMs) /
                        1000.0 / 3600.0
                durationSeconds = ((System.currentTimeMillis() - startTimeMs) / 1000)

                // Hitung kecepatan (km/jam)
                val speedKmh = if (durationHours > 0)
                    totalDistanceKm / durationHours else 0.0

                // Hitung kalori dengan use case
                val baseMet = MetValue.getBaseMet(currentActivity.name)
                totalCalories = calorieUseCase.calculate(
                    baseMet        = baseMet,
                    speedKmh       = speedKmh,
                    elevationGainM = totalElevationGain,
                    weightKg       = weightKg,
                    durationHours  = durationHours
                )
            }
        }
    }

    // Bangun notifikasi persisten
    private fun buildTrackingNotification(): Notification {
        // Buat notification channel (wajib Android 8+)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "FitRoute Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FitRoute aktif")
            .setContentText("Sedang merekam rute kamu...")
            .setSmallIcon(R.drawable.ic_logo)
            .setOngoing(true)
            .build()
    }

    // Kembalikan semua titik rute yang sudah direkam
    fun getRoutePoints(): List<LatLng> = routePoints.toList()

    override fun onDestroy() {
        super.onDestroy()
        // Hentikan update lokasi saat service dihentikan
        fusedClient.removeLocationUpdates(locationCallback)
        sensorFusion.stop()
        activityDetector.stop()
        isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
