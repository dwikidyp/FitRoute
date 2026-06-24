package com.fitroute.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorFusionManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(
        Context.SENSOR_SERVICE
    ) as SensorManager

    // Sensor barometer (tekanan udara)
    private val barometer = sensorManager
        .getDefaultSensor(Sensor.TYPE_PRESSURE)

    // Kalman filter untuk haluskan data barometer
    private val kalman = KalmanFilter1D(processNoise = 0.01)

    // Callback dipanggil saat ada data elevasi baru
    var onElevationUpdated: ((Double) -> Unit)? = null

    // Mulai baca sensor barometer
    fun start() {
        if (barometer != null) {
            sensorManager.registerListener(
                this,
                barometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    // Hentikan baca sensor
    fun stop() {
        sensorManager.unregisterListener(this)
        kalman.reset()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PRESSURE) {

            // 1. Baca tekanan udara dalam hPa
            val pressureHpa = event.values[0]

            // 2. Konversi tekanan → elevasi (meter)
            // Rumus barometrik: altitude = 44330 × [1 - (P/P₀)^0.1903]
            val rawAltitude = SensorManager.getAltitude(
                SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                pressureHpa
            ).toDouble()

            // 3. Filter dengan Kalman untuk kurangi noise
            val filtered = kalman.update(rawAltitude)

            // 4. Kirim hasil ke listener
            onElevationUpdated?.invoke(filtered)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Tidak perlu implementasi
    }

    // Cek apakah device punya barometer
    fun hasBarometer(): Boolean = barometer != null
}