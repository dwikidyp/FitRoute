package com.fitroute.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitroute.data.local.AppDatabase
import com.fitroute.data.local.WeeklyAggregate
import com.fitroute.domain.usecase.PersonalRecordUseCase
import com.fitroute.domain.usecase.PersonalRecords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class AnalyticsUiState(
    val aggregate: WeeklyAggregate = WeeklyAggregate(0.0, 0.0, 0),
    val personalRecords: PersonalRecords = PersonalRecords(),
    val isLoading: Boolean = true
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val db  = AppDatabase.getInstance(application)
    private val dao = db.workoutSessionDao()
    private val personalRecordUseCase = PersonalRecordUseCase(dao)

    // TODO: ambil dari session login
    private val userId = "user_123"

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState

    fun loadWeeklyData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Hitung rentang waktu minggu ini
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            val weekStart = cal.timeInMillis
            val weekEnd   = weekStart + 7 * 24 * 60 * 60 * 1000L

            val aggregate = dao.getWeeklyAggregate(userId, weekStart, weekEnd)
            val records   = personalRecordUseCase.getPersonalRecords(userId)

            _uiState.value = AnalyticsUiState(
                aggregate       = aggregate,
                personalRecords = records,
                isLoading       = false
            )
        }
    }

    fun loadMonthlyData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Rentang waktu bulan ini
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            val monthStart = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            val monthEnd = cal.timeInMillis

            val aggregate = dao.getWeeklyAggregate(userId, monthStart, monthEnd)
            val records   = personalRecordUseCase.getPersonalRecords(userId)

            _uiState.value = AnalyticsUiState(
                aggregate       = aggregate,
                personalRecords = records,
                isLoading       = false
            )
        }
    }
}