package com.fitroute.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ActivityDetector(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(
        Context.SENSOR_SERVICE
    ) as SensorManager

    // Sensor accelerometer & giroskop
    private val accelerometer = sensorManager
        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager
        .getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // Buffer 50 sampel (sekitar 1 detik data)
    private val accelBuffer = CircularBuffer(size = 50)

    // Variasi rotasi dari giroskop
    private var gyroVariance = 0.0

    // Callback saat aktivitas terdeteksi
    var onActivityDetected: ((ActivityType) -> Unit)? = null

    // Mulai deteksi
    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_NORMAL // ~5Hz
            )
        }
        gyroscope?.let {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    // Hentikan deteksi
    fun stop() {
        sensorManager.unregisterListener(this)
        accelBuffer.clear()
        gyroVariance = 0.0
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {

            Sensor.TYPE_ACCELEROMETER -> {
                // Hitung magnitudo percepatan (gabungan x, y, z)
                val magnitude = sqrt(
                    event.values[0].toDouble().pow(2) +
                            event.values[1].toDouble().pow(2) +
                            event.values[2].toDouble().pow(2)
                )

                // Simpan ke buffer
                accelBuffer.add(magnitude)

                // Hitung cadence (langkah per detik)
                val cadence = accelBuffer.peakFrequency()

                // Deteksi jenis aktivitas berdasarkan cadence & giroskop
                val detected = when {
                    cadence in 2.6..3.2 ->
                        ActivityType.RUNNING  // Lari: cadence tinggi

                    cadence in 1.4..2.0 ->
                        ActivityType.WALKING  // Jalan: cadence sedang

                    cadence in 1.5..2.4 && gyroVariance > 0.3 ->
                        ActivityType.CYCLING  // Sepeda: rotasi tinggi

                    else -> null
                }

                // Kirim hasil jika terdeteksi
                detected?.let {
                    onActivityDetected?.invoke(it)
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                // Hitung variasi rotasi dari giroskop
                gyroVariance = sqrt(
                    event.values[0].toDouble().pow(2) +
                            event.values[1].toDouble().pow(2) +
                            event.values[2].toDouble().pow(2)
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // Extension function untuk hitung kuadrat
    private fun Double.pow(exp: Int): Double {
        var result = 1.0
        repeat(exp) { result *= this }
        return result
    }
}