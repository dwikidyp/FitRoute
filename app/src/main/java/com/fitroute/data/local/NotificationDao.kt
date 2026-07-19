package com.fitroute.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    // Simpan notifikasi baru
    @Insert
    suspend fun insert(n: NotificationEntity)

    // Ambil semua notifikasi user, terbaru dulu
    @Query("""SELECT * FROM notifications
              WHERE userId = :uid
              ORDER BY createdAt DESC""")
    fun getAll(uid: String): Flow<List<NotificationEntity>>

    // Tandai satu notifikasi sudah dibaca
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    // Tandai semua notifikasi sudah dibaca
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :uid")
    suspend fun markAllAsRead(uid: String)

    // Hitung notifikasi yang belum dibaca
    @Query("""SELECT COUNT(*) FROM notifications
              WHERE userId = :uid AND isRead = 0""")
    fun getUnreadCount(uid: String): Flow<Int>

    // Hapus notifikasi lama (lebih dari 30 hari)
    @Query("""DELETE FROM notifications
              WHERE createdAt < :threshold""")
    suspend fun deleteOld(threshold: Long)
}