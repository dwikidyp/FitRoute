package com.fitroute.data.remote

import com.fitroute.data.local.WorkoutSessionEntity

data class SessionDto(
    val id: String,
    val user_id: String,
    val activity_type: String,
    val distance_km: Double,
    val duration_sec: Int,
    val calories_kcal: Double,
    val avg_pace: Double,
    val avg_speed_kmh: Double,
    val elev_gain_m: Double,
    val elev_loss_m: Double,
    val max_elevation_m: Double,
    val route_geo_json: String,
    val started_at: Long,
    val ended_at: Long
)

fun WorkoutSessionEntity.toDto() = SessionDto(
    id              = id,
    user_id         = userId,
    activity_type   = activityType,
    distance_km     = distanceKm,
    duration_sec    = durationSec,
    calories_kcal   = caloriesKcal,
    avg_pace        = avgPace,
    avg_speed_kmh   = avgSpeedKmh,
    elev_gain_m     = elevGainM,
    elev_loss_m     = elevLossM,
    max_elevation_m = maxElevationM,
    route_geo_json  = routeGeoJson,
    started_at      = startedAt,
    ended_at        = endedAt
)