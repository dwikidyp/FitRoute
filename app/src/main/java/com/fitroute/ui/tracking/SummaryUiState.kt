package com.fitroute.ui.tracking

import com.fitroute.data.local.WorkoutSessionEntity

data class SummaryUiState(
    val session: WorkoutSessionEntity? = null,
    val isPersonalRecord: Boolean = false,
    val pacePerKm: List<Double> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)