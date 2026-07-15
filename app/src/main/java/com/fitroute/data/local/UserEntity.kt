package com.fitroute.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val age: Int,
    val weightKg: Float,
    val heightCm: Float,
    val gender: String,
    val activityPref: String,
    val deviceUid: String
)