package com.fitroute.ui.history

import com.fitroute.data.local.WorkoutSessionEntity

data class DetailUiState(
    val session: WorkoutSessionEntity? = null,
    val isLoading: Boolean = true,
    val navigateBack: Boolean = false,
    val error: String? = null
)