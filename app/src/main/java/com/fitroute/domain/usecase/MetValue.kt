package com.fitroute.domain.usecase

object MetValue {

    // Nilai MET dasar per jenis aktivitas
    const val WALKING = 3.5
    const val RUNNING = 9.8
    const val CYCLING = 7.5
    const val HIKING  = 6.0

    // Ambil MET berdasarkan jenis aktivitas
    fun getBaseMet(activityType: String): Double {
        return when (activityType.uppercase()) {
            "WALKING" -> WALKING
            "RUNNING" -> RUNNING
            "CYCLING" -> CYCLING
            "HIKING"  -> HIKING
            else      -> WALKING
        }
    }
}