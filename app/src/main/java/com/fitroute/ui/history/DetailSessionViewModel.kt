package com.fitroute.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitroute.data.local.AppDatabase
import com.fitroute.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val sessionRepository = SessionRepository(db.workoutSessionDao())

    private val _detailState = MutableStateFlow(DetailUiState())
    val detailState: StateFlow<DetailUiState> = _detailState

    // Muat detail sesi berdasarkan ID
    fun loadDetail(sessionId: String) {
        viewModelScope.launch {
            try {
                val session = sessionRepository.getById(sessionId)
                _detailState.value = DetailUiState(
                    session   = session,
                    isLoading = false
                )
            } catch (e: Exception) {
                _detailState.value = DetailUiState(
                    isLoading = false,
                    error     = e.message
                )
            }
        }
    }

    // Hapus sesi dan kembali ke riwayat
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
            _detailState.value = _detailState.value.copy(
                navigateBack = true
            )
        }
    }
}