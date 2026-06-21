package com.fitroute.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.fitroute.data.repository.UserRepository
import kotlinx.coroutines.launch

// Sealed class Result
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// Sealed class AuthState
sealed class AuthState {
    object LoggedIn : AuthState()
    object LoggedOut : AuthState()
}

// Data class response token
data class AuthResponse(val token: String)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _loginResult = MutableLiveData<Result<AuthResponse>>()
    val loginResult: LiveData<Result<AuthResponse>> = _loginResult

    private val _registerResult = MutableLiveData<Result<Unit>>()
    val registerResult: LiveData<Result<Unit>> = _registerResult

    private val _biometricEnrollResult = MutableLiveData<Result<Unit>>()
    val biometricEnrollResult: LiveData<Result<Unit>> = _biometricEnrollResult

    private val securePrefs = EncryptedSharedPreferences.create(
        application,
        "secure_prefs",
        MasterKey.Builder(application)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Cek sesi login sebelumnya
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

    // Login dengan email & password
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.postValue(Result.Loading)
            try {
                // TODO: Ganti dengan API call Retrofit
                // val response = authRepository.login(email, password)
                // Simulasi sukses:
                val fakeToken = "eyJhbGciOiJIUzI1NiJ9.fake_token"
                _loginResult.postValue(Result.Success(AuthResponse(fakeToken)))
            } catch (e: Exception) {
                _loginResult.postValue(Result.Error(e.message ?: "Login gagal"))
            }
        }
    }

    // Registrasi user baru
    fun register(user: UserRequest) {
        viewModelScope.launch {
            _registerResult.postValue(Result.Loading)
            try {
                // TODO: Implementasi registrasi dengan Retrofit
                // Simulasi sukses:
                _registerResult.postValue(Result.Success(Unit))
            } catch (e: Exception) {
                _registerResult.postValue(Result.Error(e.message ?: "Registrasi gagal"))
            }
        }
    }

    // Login dengan biometric (gunakan token tersimpan)
    fun loginWithBiometric() {
        viewModelScope.launch {
            val token = securePrefs.getString("auth_token", null)
            if (token != null) {
                _loginResult.postValue(Result.Success(AuthResponse(token)))
            } else {
                _loginResult.postValue(Result.Error("Token tidak ditemukan, login dengan email"))
            }
        }
    }

    fun enrollBiometric(publicKey: String) {
        viewModelScope.launch {
            _biometricEnrollResult.postValue(Result.Loading)
            try {
                // TODO: Kirim public key ke server
                _biometricEnrollResult.postValue(Result.Success(Unit))
            } catch (e: Exception) {
                _biometricEnrollResult.postValue(Result.Error(e.message ?: "Enrollment gagal"))
            }
        }
    }

    // Simpan token ke EncryptedSharedPreferences
    fun saveToken(token: String) {
        securePrefs.edit().putString("auth_token", token).apply()
    }

    private fun isTokenExpired(token: String): Boolean {
        return false
    }

    private val userRepository = UserRepository()

    // Fungsi logout
    fun logout() {
        viewModelScope.launch {
            // 1. Hapus token dari EncryptedSharedPrefs
            securePrefs.edit()
                .remove("auth_token")
                .apply()

            // 2. Hapus data lokal pengguna
            userRepository.clearLocalData()

            // 3. Kembali ke halaman login
            _authState.postValue(AuthState.LoggedOut)
        }
    }
}
