package com.fitroute.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
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
import com.fitroute.util.SensorFusionManager

class TrackingService : Service() {

    // Client GPS dari Google Play Services
    private lateinit var fusedClient: FusedLocationProviderClient

    // Daftar titik koordinat rute
    private val routePoints = mutableListOf<LatLng>()

    private lateinit var sensorFusion: SensorFusionManager
    private var currentElevation = 0.0
    private var totalElevationGain = 0.0
    private var lastElevation = 0.0

    companion object {
        const val CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_ID = 1

        // Untuk komunikasi dengan Fragment
        var isRunning = false
        var latestLocation: LatLng? = null
        var latestElevation = 0.0
        var totalElevationGain = 0.0
    }

    override fun onCreate() {
        super.onCreate()

        // 1. Tampilkan notifikasi agar service tidak dibunuh Android
        startForeground(NOTIFICATION_ID, buildTrackingNotification())

        // 2. Inisialisasi GPS client
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

        // 3. Mulai terima update lokasi
        startLocationUpdates()

        isRunning = true
    }

    private fun startLocationUpdates() {
        // Request lokasi setiap 2 detik, akurasi tinggi
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L // interval 2 detik
        )
            .setMinUpdateDistanceMeters(3f) // update jika bergerak minimal 3 meter
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
            }
        }
    }

    // Bangun notifikasi persisten
    private fun buildTrackingNotification(): Notification {
        // Buat notification channel (wajib Android 8+)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "FitRoute Tracking",
            NotificationManager.IMPORTANCE_LOW // LOW = tidak ada suara
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FitRoute aktif")
            .setContentText("Sedang merekam rute kamu...")
            .setSmallIcon(R.drawable.ic_logo)
            .setOngoing(true) // tidak bisa diswipe
            .build()
    }

    // Kembalikan semua titik rute yang sudah direkam
    fun getRoutePoints(): List<LatLng> = routePoints.toList()

    override fun onDestroy() {
        super.onDestroy()
        // Hentikan update lokasi saat service dihentikan
        fusedClient.removeLocationUpdates(locationCallback)
        isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
