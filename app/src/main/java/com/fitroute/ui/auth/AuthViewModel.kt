package com.fitroute.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.launch

// Sealed class untuk status autentikasi
sealed class AuthState {
    object LoggedIn : AuthState()
    object LoggedOut : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    // Inisialisasi EncryptedSharedPreferences
    private val securePrefs = EncryptedSharedPreferences.create(
        application,
        "secure_prefs",
        MasterKey.Builder(application)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun checkSession() {
        viewModelScope.launch {
            val token = securePrefs.getString("auth_token", null)

            if (token != null && !isTokenExpired(token)) {
                _authState.postValue(AuthState.LoggedIn)
            } else {
                _authState.postValue(AuthState.LoggedOut)
            }
        }
    }

    // Cek apakah token sudah expired
    private fun isTokenExpired(token: String): Boolean {
        // Implementasi logika cek expiry token
        // Contoh sederhana: selalu valid (sesuaikan dengan JWT/logika kamu)
        return false
    }
}