package com.fitroute.util

class KalmanFilter1D(private val processNoise: Double) {

    private var estimate = 0.0
    private var errorCovariance = 1.0

    fun update(
        measurement: Double,
        measurementNoise: Double = 0.5
    ): Double {
        // Prediksi error bertambah seiring waktu
        errorCovariance += processNoise

        // Hitung Kalman Gain
        val kalmanGain = errorCovariance / (errorCovariance + measurementNoise)

        // Update estimasi dengan data baru
        estimate += kalmanGain * (measurement - estimate)

        // Update error covariance
        errorCovariance *= (1 - kalmanGain)

        return estimate
    }

    // Reset filter
    fun reset() {
        estimate = 0.0
        errorCovariance = 1.0
    }
}