package com.fitroute.domain.usecase

class CalorieCalculationUseCase {

    /**
     * Hitung kalori dengan MET adaptif
     *
     *
     * @param baseMet        nilai MET dasar sesuai aktivitas
     * @param speedKmh       kecepatan rata-rata (km/jam)
     * @param elevationGainM total elevasi naik (meter)
     * @param weightKg       berat badan pengguna (kg)
     * @param durationHours  durasi aktivitas (jam)
     */
    fun calculate(
        baseMet: Double,
        speedKmh: Double,
        elevationGainM: Double,
        weightKg: Double,
        durationHours: Double
    ): Double {

        // MET naik jika kecepatan di atas rata-rata jalan (8 km/jam)
        val speedFactor = if (speedKmh > 8.0)
            1.0 + (speedKmh - 8.0) * 0.05
        else 1.0

        // MET naik 0.5 per 100m elevasi gain per jam
        val elevationFactor = if (durationHours > 0)
            1.0 + (elevationGainM / 100.0) * 0.5 / durationHours
        else 1.0

        // MET adaptif = MET dasar × faktor kecepatan × faktor elevasi
        val adaptiveMet = baseMet * speedFactor * elevationFactor

        // Kalori = MET adaptif × berat × durasi
        return adaptiveMet * weightKg * durationHours
    }
}