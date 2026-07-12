package com.fitroute.data.local

// Data class hasil query agregasi dari Room
data class WeeklyAggregate(
    val totalDistance: Double,
    val totalCalories: Double,
    val sessionCount: Int
)