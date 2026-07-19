package com.fitroute.data.repository

import com.fitroute.data.local.NotificationDao
import com.fitroute.data.local.NotificationEntity
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val dao: NotificationDao) {

    // Ambil semua notifikasi
    fun getAll(userId: String): Flow<List<NotificationEntity>> =
        dao.getAll(userId)

    // Hitung yang belum dibaca
    fun getUnreadCount(userId: String): Flow<Int> =
        dao.getUnreadCount(userId)

    // Buat notifikasi baru
    suspend fun create(
        userId: String,
        type: String,
        title: String,
        body: String
    ) {
        dao.insert(
            NotificationEntity(
                userId = userId,
                type   = type,
                title  = title,
                body   = body
            )
        )
    }

    // Tandai sudah dibaca
    suspend fun markAsRead(id: String) = dao.markAsRead(id)

    // Tandai semua sudah dibaca
    suspend fun markAllAsRead(userId: String) = dao.markAllAsRead(userId)

    // Hapus notifikasi lama (> 30 hari)
    suspend fun deleteOld() {
        val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        dao.deleteOld(threshold)
    }
}