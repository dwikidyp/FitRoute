package com.fitroute.domain.usecase

import com.fitroute.data.local.WorkoutSessionEntity
import java.util.UUID

data class SessionSummary(
    val userId: String,
    val activityType: String,
    val distanceKm: Double,
    val durationSec: Int,
    val caloriesKcal: Double,
    val avgPace: Double,
    val avgSpeedKmh: Double,
    val elevGainM: Double,
    val elevLossM: Double,
    val maxElevationM: Double,
    val routeGeoJson: String,
    val startedAt: Long,
    val endedAt: Long
) {
    // Konversi ke Entity untuk disimpan ke Room
    fun toEntity(): WorkoutSessionEntity {
        return WorkoutSessionEntity(
            id            = UUID.randomUUID().toString(),
            userId        = userId,
            activityType  = activityType,
            distanceKm    = distanceKm,
            durationSec   = durationSec,
            caloriesKcal  = caloriesKcal,
            avgPace       = avgPace,
            avgSpeedKmh   = avgSpeedKmh,
            elevGainM     = elevGainM,
            elevLossM     = elevLossM,
            maxElevationM = maxElevationM,
            routeGeoJson  = routeGeoJson,
            startedAt     = startedAt,
            endedAt       = endedAt,
            isSynced      = false
        )
    }
}