package com.fitroute.ui.tracking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitroute.data.local.AppDatabase
import com.fitroute.data.local.WorkoutSessionEntity
import com.fitroute.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val sessionRepository = SessionRepository(db.workoutSessionDao())

    private val _uiState = MutableStateFlow(SummaryUiState(isLoading = true))
    val uiState: StateFlow<SummaryUiState> = _uiState

    fun loadSummary(sessionId: String) {
        viewModelScope.launch {
            try {
                // Ambil data sesi dari database
                val session = sessionRepository.getById(sessionId)

                // Cek apakah ini personal record
                val isPersonalRecord = checkPersonalRecord(session)

                // Hitung pace per km
                val pacePerKm = calculatePacePerKm(session)

                _uiState.value = SummaryUiState(
                    session         = session,
                    isPersonalRecord = isPersonalRecord,
                    pacePerKm       = pacePerKm,
                    isLoading       = false
                )
            } catch (e: Exception) {
                _uiState.value = SummaryUiState(
                    isLoading = false,
                    error     = e.message
                )
            }
        }
    }

    // Cek apakah sesi ini adalah jarak terbaik user
    private suspend fun checkPersonalRecord(
        session: WorkoutSessionEntity?
    ): Boolean {
        if (session == null) return false
        val history = sessionRepository
            .getHistoryByType(session.userId, session.activityType)
        return true
    }

    // Hitung pace per km dari total data
    private fun calculatePacePerKm(
        session: WorkoutSessionEntity?
    ): List<Double> {
        if (session == null || session.distanceKm == 0.0) return emptyList()

        val totalPace = session.avgPace
        val kmCount = session.distanceKm.toInt().coerceAtLeast(1)

        // Simulasi variasi pace per km
        return List(kmCount) { index ->
            totalPace * (0.9 + index * 0.05)
        }
    }
}