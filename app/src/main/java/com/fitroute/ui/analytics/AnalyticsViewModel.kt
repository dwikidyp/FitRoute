package com.fitroute.ui.analytics

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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
    val isLoading: Boolean = true,
    val isLoggedOut: Boolean = false
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val db  = AppDatabase.getInstance(application)
    private val dao = db.workoutSessionDao()
    private val personalRecordUseCase = PersonalRecordUseCase(dao)

    private val userId: String? = getUserIdFromSession(application)

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState

    private fun getUserIdFromSession(context: Context): String? {
        return try {
            val securePrefs = EncryptedSharedPreferences.create(
                context,
                "secure_prefs",
                MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            securePrefs.getString("user_id", null)

        } catch (e: Exception) {
            null
        }
    }


    fun loadWeeklyData() {
        val uid = userId ?: run {
            _uiState.value = _uiState.value.copy(
                isLoading  = false,
                isLoggedOut = true
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            val weekStart = cal.timeInMillis
            val weekEnd   = weekStart + 7 * 24 * 60 * 60 * 1000L

            val aggregate = dao.getWeeklyAggregate(uid, weekStart, weekEnd)
            val records   = personalRecordUseCase.getPersonalRecords(uid)

            _uiState.value = AnalyticsUiState(
                aggregate       = aggregate,
                personalRecords = records,
                isLoading       = false
            )
        }
    }

    fun loadMonthlyData() {
        val uid = userId ?: run {
            _uiState.value = _uiState.value.copy(
                isLoading   = false,
                isLoggedOut = true
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            val monthStart = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            val monthEnd = cal.timeInMillis

            val aggregate = dao.getWeeklyAggregate(uid, monthStart, monthEnd)
            val records   = personalRecordUseCase.getPersonalRecords(uid)

            _uiState.value = AnalyticsUiState(
                aggregate       = aggregate,
                personalRecords = records,
                isLoading       = false
            )
        }
    }
}