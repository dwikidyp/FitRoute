package com.fitroute.domain.usecase

import com.fitroute.data.local.WorkoutSessionDao
import com.fitroute.data.local.WorkoutSessionEntity

data class PersonalRecords(
    val longestDistanceKm: Double = 0.0,
    val longestDistanceActivity: String = "",
    val longestDistanceType: String = "",

    val highestCalories: Double = 0.0,
    val highestCaloriesActivity: String = "",

    val highestElevationM: Double = 0.0,
    val highestElevationActivity: String = "",

    val bestPaceMinKm: Double = 0.0,
    val bestPaceActivity: String = ""
)

class PersonalRecordUseCase(private val dao: WorkoutSessionDao) {

    // Ambil semua personal record user
    suspend fun getPersonalRecords(userId: String): PersonalRecords {

        // Cek tiap jenis aktivitas untuk jarak terpanjang
        val runSession  = dao.getLongestSession(userId, "RUNNING")
        val bikeSession = dao.getLongestSession(userId, "CYCLING")
        val hikeSession = dao.getLongestSession(userId, "HIKING")

        // Cari yang terpanjang dari semua aktivitas
        val longestSession = listOfNotNull(runSession, bikeSession, hikeSession)
            .maxByOrNull { it.distanceKm }

        // Kalori terbanyak
        val highestCalSession = dao.getHighestCaloriesSession(userId)

        // Elevasi tertinggi
        val maxElev = dao.getMaxElevation(userId) ?: 0.0
        val elevSession = listOfNotNull(runSession, bikeSession, hikeSession)
            .maxByOrNull { it.elevGainM }

        // Pace terbaik
        val bestPace = dao.getBestPace(userId) ?: 0.0

        return PersonalRecords(
            longestDistanceKm       = longestSession?.distanceKm ?: 0.0,
            longestDistanceActivity = formatActivity(longestSession?.activityType),
            longestDistanceType     = longestSession?.activityType ?: "",

            highestCalories         = highestCalSession?.caloriesKcal ?: 0.0,
            highestCaloriesActivity = formatActivity(highestCalSession?.activityType),

            highestElevationM       = maxElev,
            highestElevationActivity = formatActivity(elevSession?.activityType),

            bestPaceMinKm           = bestPace,
            bestPaceActivity        = "Lari"
        )
    }

    // Cek apakah sesi baru memecahkan rekor jarak
    suspend fun checkAndUpdate(session: WorkoutSessionEntity): Boolean {
        val prevMax = dao.getMaxDistance(
            session.userId,
            session.activityType
        ) ?: 0.0

        return session.distanceKm > prevMax
    }

    private fun formatActivity(type: String?): String {
        return when (type) {
            "RUNNING" -> "Lari"
            "CYCLING" -> "Sepeda"
            "HIKING"  -> "Hiking"
            else      -> type ?: "-"
        }
    }
}