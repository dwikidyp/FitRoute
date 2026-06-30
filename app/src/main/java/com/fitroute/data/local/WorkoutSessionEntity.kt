package com.fitroute.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val userId: String,
    val activityType: String,    // RUNNING, CYCLING, HIKING

    val distanceKm: Double,
    val durationSec: Int,
    val caloriesKcal: Double,

    val avgPace: Double,         // menit per km
    val avgSpeedKmh: Double,

    val elevGainM: Double,       // total elevasi naik
    val elevLossM: Double,       // total elevasi turun
    val maxElevationM: Double,

    val routeGeoJson: String,    // rute disimpan sebagai JSON string

    val startedAt: Long,         // timestamp mulai
    val endedAt: Long,           // timestamp selesai

    val isSynced: Boolean = false // status sinkronisasi ke server
)