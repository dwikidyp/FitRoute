package com.fitroute.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitroute.data.local.AppDatabase
import com.fitroute.data.local.UserEntity
import com.fitroute.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.pow

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db             = AppDatabase.getInstance(application)
    private val userRepository = UserRepository(db.userDao())

    // TODO: ambil dari session login
    private val userId = "user_123"

    // Profil user + BMI (reaktif)
    val profileState = userRepository.getUser(userId)
        .map { user ->
            user?.let {
                // Hitung BMI: berat (kg) / tinggi (m)²
                val heightM = it.heightCm / 100f
                val bmi     = it.weightKg / heightM.pow(2)

                val bmiCategory = when {
                    bmi < 18.5f -> "Kurang"
                    bmi < 25.0f -> "Normal"
                    bmi < 30.0f -> "Lebih"
                    else        -> "Obesitas"
                }

                ProfileUiState(
                    user        = it,
                    bmi         = bmi,
                    bmiCategory = bmiCategory,
                    isLoading   = false
                )
            } ?: ProfileUiState(isLoading = false)
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState()
        )

    // Simpan perubahan profil
    fun saveProfile(updated: UserEntity) {
        viewModelScope.launch {
            userRepository.updateUser(updated)
            // TODO: sinkronisasi ke Supabase
            // userApiService.syncProfile(updated)
        }
    }

    // Buat profil dummy untuk testing
    fun createDummyProfile() {
        viewModelScope.launch {
            val dummy = UserEntity(
                id           = userId,
                fullName     = "Dwiki Dzaki",
                email        = "dwiki@gmail.com",
                age          = 22,
                weightKg     = 70f,
                heightCm     = 170f,
                gender       = "Laki-laki",
                activityPref = "RUNNING",
                deviceUid    = "device_123"
            )
            userRepository.insertUser(dummy)
        }
    }
}