package com.fitroute.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fitroute.data.local.AppDatabase
import com.fitroute.data.remote.RetrofitClient
import com.fitroute.data.remote.toDto
import com.fitroute.data.repository.NotificationRepository

class SyncWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return try {
            val db         = AppDatabase.getInstance(applicationContext)
            val sessionDao = db.workoutSessionDao()
            val notifRepo  = NotificationRepository(db.notificationDao())

            // Ambil sesi yang belum tersinkron
            val unsynced = sessionDao.getUnsyncedSessions()

            if (unsynced.isNotEmpty()) {
                // Kirim ke Supabase
                val apiService = RetrofitClient.create(
                    applicationContext.getSharedPreferences(
                        "secure_prefs", Context.MODE_PRIVATE
                    )
                )

                val response = apiService.uploadSessions(
                    unsynced.map { it.toDto() }
                )

                if (response.isSuccessful) {
                    // Tandai semua sudah tersinkron
                    unsynced.forEach { session ->
                        sessionDao.markAsSynced(session.id)
                    }

                    // Buat notifikasi berhasil
                    notifRepo.create(
                        userId = unsynced.first().userId,
                        type   = "SYNC",
                        title  = "Sinkronisasi berhasil",
                        body   = "${unsynced.size} sesi tersimpan ke cloud"
                    )
                } else {
                    return Result.retry()
                }
            }

            Result.success()

        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {

        // Jadwalkan sync satu kali
        fun scheduleOneTime(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(
                    androidx.work.NetworkType.CONNECTED
                )
                .build()

            val request = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            androidx.work.WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "fitroute_sync",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    request
                )
        }

        // Jadwalkan sync berkala setiap 6 jam
        fun schedulePeriodic(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(
                    androidx.work.NetworkType.CONNECTED
                )
                .build()

            val request = androidx.work.PeriodicWorkRequestBuilder<SyncWorker>(
                6, java.util.concurrent.TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            androidx.work.WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "fitroute_sync_periodic",
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}