package com.fitroute.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitroute.data.local.AppDatabase
import com.fitroute.data.repository.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val sessionRepository = SessionRepository(db.workoutSessionDao())

    // TODO: ambil userId dari session login yang tersimpan
    private val userId = "user_123"

    // Filter aktif saat ini
    private val activeFilter = MutableStateFlow(ActivityFilter.ALL)

    // Daftar sesi berdasarkan filter
    val historyList = activeFilter
        .flatMapLatest { filter ->
            when (filter) {
                ActivityFilter.ALL ->
                    sessionRepository.getHistory(userId)
                else ->
                    sessionRepository.getHistoryByType(userId, filter.apiValue)
            }
        }
        .map { sessions -> sessions.toUiModels() }
        .stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5000),
            initialValue   = emptyList()
        )

    // Ubah filter
    fun onFilterSelected(filter: ActivityFilter) {
        activeFilter.value = filter
    }
}