package com.fitroute.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    // Simpan sesi baru
    @Insert
    suspend fun insert(session: WorkoutSessionEntity)

    // Ambil 1 sesi berdasarkan ID
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getById(id: String): WorkoutSessionEntity?

    // Ambil semua riwayat sesi user
    @Query("""SELECT * FROM workout_sessions
        WHERE userId = :userId
        ORDER BY startedAt DESC""")
    fun getHistory(userId: String): Flow<List<WorkoutSessionEntity>>

    // Ambil riwayat berdasarkan jenis aktivitas
    @Query("""SELECT * FROM workout_sessions
        WHERE userId = :userId AND activityType = :type
        ORDER BY startedAt DESC""")
    fun getHistoryByType(userId: String, type: String): Flow<List<WorkoutSessionEntity>>

    // Ambil sesi yang belum tersinkron ke server
    @Query("SELECT * FROM workout_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<WorkoutSessionEntity>

    // Update status sinkronisasi
    @Query("UPDATE workout_sessions SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    // Hapus 1 sesi
    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun delete(id: String)

    // Hapus semua riwayat user
    @Query("DELETE FROM workout_sessions WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
}