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

    // Total jarak, kalori, jumlah sesi dalam rentang waktu
    @Query("""SELECT SUM(distanceKm) as totalDistance,
                    SUM(caloriesKcal) as totalCalories,
                    COUNT(*) as sessionCount
             FROM workout_sessions
             WHERE userId = :userId
             AND startedAt BETWEEN :weekStart AND :weekEnd""")
    suspend fun getWeeklyAggregate(
        userId: String,
        weekStart: Long,
        weekEnd: Long
    ): WeeklyAggregate

    // Jarak maksimum per aktivitas (untuk personal record)
    @Query("""SELECT MAX(distanceKm) FROM workout_sessions
              WHERE userId = :userId AND activityType = :type""")
    suspend fun getMaxDistance(userId: String, type: String): Double?

    // Kalori maksimum
    @Query("""SELECT MAX(caloriesKcal) FROM workout_sessions
              WHERE userId = :userId""")
    suspend fun getMaxCalories(userId: String): Double?

    // Elevasi tertinggi
    @Query("""SELECT MAX(elevGainM) FROM workout_sessions
              WHERE userId = :userId""")
    suspend fun getMaxElevation(userId: String): Double?

    // Pace terbaik (nilai terkecil = tercepat)
    @Query("""SELECT MIN(avgPace) FROM workout_sessions
              WHERE userId = :userId AND activityType = 'RUNNING'""")
    suspend fun getBestPace(userId: String): Double?

    // Ambil sesi dengan jarak terpanjang
    @Query("""SELECT * FROM workout_sessions
              WHERE userId = :userId AND activityType = :type
              ORDER BY distanceKm DESC LIMIT 1""")
    suspend fun getLongestSession(userId: String, type: String): WorkoutSessionEntity?

    // Ambil sesi dengan kalori terbanyak
    @Query("""SELECT * FROM workout_sessions
              WHERE userId = :userId
              ORDER BY caloriesKcal DESC LIMIT 1""")
    suspend fun getHighestCaloriesSession(userId: String): WorkoutSessionEntity?
}