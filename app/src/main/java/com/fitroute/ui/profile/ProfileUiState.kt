package com.fitroute.ui.profile

import com.fitroute.data.local.UserEntity

data class ProfileUiState(
    val user: UserEntity? = null,
    val bmi: Float = 0f,
    val bmiCategory: String = "",
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
)