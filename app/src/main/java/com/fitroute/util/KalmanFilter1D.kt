package com.fitroute.util

class KalmanFilter1D(private val processNoise: Double) {

    private var estimate = 0.0
    private var errorCovariance = 1.0

    fun update(
        measurement: Double,
        measurementNoise: Double = 0.5
    ): Double {
        // 1. Prediksi error bertambah seiring waktu
        errorCovariance += processNoise

        // 2. Hitung Kalman Gain (bobot antara estimasi vs pengukuran)
        val kalmanGain = errorCovariance / (errorCovariance + measurementNoise)

        // 3. Update estimasi dengan data baru
        estimate += kalmanGain * (measurement - estimate)

        // 4. Update error covariance
        errorCovariance *= (1 - kalmanGain)

        return estimate
    }

    // Reset filter (misal saat mulai tracking baru)
    fun reset() {
        estimate = 0.0
        errorCovariance = 1.0
    }
}