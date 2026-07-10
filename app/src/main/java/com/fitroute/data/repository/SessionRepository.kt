package com.fitroute.data.repository

import com.fitroute.data.local.WorkoutSessionDao
import com.fitroute.data.local.WorkoutSessionEntity
import com.fitroute.domain.usecase.SessionSummary
import kotlinx.coroutines.flow.Flow

class SessionRepository(
    private val sessionDao: WorkoutSessionDao
) {
    // Simpan sesi ke Room Database
    suspend fun saveSession(summary: SessionSummary): String {
        val entity = summary.toEntity()
        sessionDao.insert(entity)
        return entity.id
    }

    // Ambil 1 sesi berdasarkan ID
    suspend fun getById(sessionId: String): WorkoutSessionEntity? {
        return sessionDao.getById(sessionId)
    }

    // Ambil semua riwayat sesi
    fun getHistory(userId: String): Flow<List<WorkoutSessionEntity>> {
        return sessionDao.getHistory(userId)
    }

    // Ambil riwayat berdasarkan jenis aktivitas
    fun getHistoryByType(userId: String, type: String): Flow<List<WorkoutSessionEntity>> {
        return sessionDao.getHistoryByType(userId, type)
    }

    // Hapus semua data user (untuk logout)
    suspend fun clearUserData(userId: String) {
        sessionDao.deleteAllByUser(userId)
    }

    // Hapus satu sesi berdasarkan ID
    suspend fun deleteSession(sessionId: String) {
        sessionDao.delete(sessionId)
    }
}