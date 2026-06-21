package com.fitroute.ui.auth

data class UserRequest(
    val name: String,
    val email: String,
    val password: String,
    val age: Int,
    val weightKg: Float,
    val heightCm: Float,
    val gender: String,
    val deviceUid: String
)