package com.fitroute.ui.tracking

data class LiveStatsUiState(
    val distanceKm: Double = 0.0,
    val elevationGainM: Double = 0.0,
    val calories: Double = 0.0,
    val pace: Double = 0.0,
    val durationFormatted: String = "00:00:00"
) {
    companion object {
        val Empty = LiveStatsUiState()
    }
}