package com.fitroute.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AppSettings(context: Context) {

    // Semua preferensi dienkripsi AES-256
    private val prefs: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "fitroute_settings",
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback jika enkripsi gagal
        context.getSharedPreferences("fitroute_settings_fb", Context.MODE_PRIVATE)
    }

    // ===== PENGATURAN SENSOR =====

    // Aktifkan login sidik jari
    var fingerprintEnabled: Boolean
        get()    = prefs.getBoolean("fingerprint_enabled", false)
        set(v)   { prefs.edit().putBoolean("fingerprint_enabled", v).apply() }

    // Aktifkan barometer untuk elevasi
    var barometerEnabled: Boolean
        get()    = prefs.getBoolean("barometer_enabled", true)
        set(v)   { prefs.edit().putBoolean("barometer_enabled", v).apply() }

    // GPS akurasi tinggi
    var gpsHighAccuracy: Boolean
        get()    = prefs.getBoolean("gps_high_accuracy", true)
        set(v)   { prefs.edit().putBoolean("gps_high_accuracy", v).apply() }

    // Deteksi aktivitas otomatis
    var autoDetectActivity: Boolean
        get()    = prefs.getBoolean("auto_detect_activity", true)
        set(v)   { prefs.edit().putBoolean("auto_detect_activity", v).apply() }

    // ===== PENGATURAN NOTIFIKASI =====

    // Notifikasi personal record
    var notifyPersonalRecord: Boolean
        get()    = prefs.getBoolean("notify_pr", true)
        set(v)   { prefs.edit().putBoolean("notify_pr", v).apply() }

    // Notifikasi target mingguan
    var notifyWeeklyGoal: Boolean
        get()    = prefs.getBoolean("notify_weekly", true)
        set(v)   { prefs.edit().putBoolean("notify_weekly", v).apply() }

    // ===== PENGATURAN TARGET =====

    // Target jarak mingguan
    var weeklyDistanceTarget: Float
        get()    = prefs.getFloat("weekly_target_km", 50f)
        set(v)   { prefs.edit().putFloat("weekly_target_km", v).apply() }

    // Reset semua pengaturan ke default
    fun resetToDefault() {
        prefs.edit().clear().apply()
    }
}