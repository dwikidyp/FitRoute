package com.fitroute.ui.tracking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitroute.domain.usecase.CalorieCalculationUseCase
import com.fitroute.domain.usecase.MetValue
import com.fitroute.service.TrackingService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LiveTrackingViewModel(application: Application) : AndroidViewModel(application) {

    private val calorieUseCase = CalorieCalculationUseCase()

    private val _liveStats = MutableLiveData(LiveStatsUiState.Empty)
    val liveStats: LiveData<LiveStatsUiState> = _liveStats

    private val _navigateToSummary = MutableLiveData(false)
    val navigateToSummary: LiveData<Boolean> = _navigateToSummary

    // Status tracking
    private val _isPaused = MutableLiveData(false)
    val isPaused: LiveData<Boolean> = _isPaused

    // Mulai observe data dari TrackingService setiap 1 detik
    fun startObserving(weightKg: Double, activityType: String) {
        viewModelScope.launch {
            while (TrackingService.isRunning) {
                val distanceKm    = TrackingService.totalDistanceKm
                val elevationGain = TrackingService.totalElevationGain
                val durationSec   = TrackingService.durationSeconds
                val durationHours = durationSec / 3600.0

                // Hitung kalori
                val baseMet = MetValue.getBaseMet(activityType)
                val speedKmh = if (durationHours > 0)
                    distanceKm / durationHours else 0.0
                val calories = calorieUseCase.calculate(
                    baseMet        = baseMet,
                    speedKmh       = speedKmh,
                    elevationGainM = elevationGain,
                    weightKg       = weightKg,
                    durationHours  = durationHours
                )

                // Hitung pace
                val pace = if (distanceKm > 0)
                    durationHours * 60 / distanceKm else 0.0

                // Update UI state
                _liveStats.postValue(
                    LiveStatsUiState(
                        distanceKm        = distanceKm,
                        elevationGainM    = elevationGain,
                        calories          = calories,
                        pace              = pace,
                        durationFormatted = durationSec.toFormattedTime()
                    )
                )

                delay(1000)
            }
        }
    }

    // Pause tracking
    fun pauseSession() {
        _isPaused.value = true
        // TODO: TrackingService.pause()
    }

    // Resume tracking
    fun resumeSession() {
        _isPaused.value = false
        // TODO: TrackingService.resume()
    }

    // Stop dan simpan sesi
    fun stopSession() {
        viewModelScope.launch {
            TrackingService.isRunning = false
            // TODO: sessionRepository.saveSession(buildSummary())
            _navigateToSummary.postValue(true)
        }
    }
}

// Extension: konversi detik ke format HH:MM:SS
fun Long.toFormattedTime(): String {
    val jam   = this / 3600
    val menit = (this % 3600) / 60
    val detik = this % 60
    return "%02d:%02d:%02d".format(jam, menit, detik)
}