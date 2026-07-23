package com.fitroute.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.fitroute.data.local.AppDatabase
import com.fitroute.data.remote.RetrofitClient
import com.fitroute.data.repository.AuthRepository
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
data class AuthResponse(val token: String, val userId: String)

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

    // Repository
    private val database = AppDatabase.getInstance(application)
    private val userRepository = UserRepository(database.userDao())
    private val authRepository = AuthRepository(RetrofitClient.authApiService)

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

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.postValue(Result.Loading)
            val result = authRepository.login(email, password)
            _loginResult.postValue(result)

            if (result is Result.Success) {
                saveToken(result.data.token)
                saveUserId(result.data.userId)
            }
        }
    }

    private fun saveUserId(userId: String) {
        securePrefs.edit().putString("user_id", userId).apply()
    }

    // Registrasi user baru
    fun register(user: UserRequest) {
        viewModelScope.launch {
            _registerResult.postValue(Result.Loading)
            val result = authRepository.register(user.email, user.password)
            
            if (result is Result.Success) {
                saveToken(result.data.token)
                saveUserId(result.data.userId)
                _registerResult.postValue(Result.Success(Unit))
            } else if (result is Result.Error) {
                _registerResult.postValue(Result.Error(result.message))
            }
        }
    }

    // Login dengan biometric (gunakan token tersimpan)
    fun loginWithBiometric() {
        viewModelScope.launch {
            val token = securePrefs.getString("auth_token", null)
            val userId = securePrefs.getString("user_id", "") ?: ""
            if (token != null) {
                _loginResult.postValue(Result.Success(AuthResponse(token, userId)))
            } else {
                _loginResult.postValue(Result.Error("Token tidak ditemukan, login dengan email"))
            }
        }
    }

    fun enrollBiometric(publicKey: String) {
        viewModelScope.launch {
            _biometricEnrollResult.postValue(Result.Loading)
            val deviceUid = securePrefs.getString("device_uid", "unknown") ?: "unknown"
            val result = authRepository.enrollBiometric(publicKey, deviceUid)
            
            if (result is Result.Success) {
                _biometricEnrollResult.postValue(Result.Success(Unit))
            } else if (result is Result.Error) {
                _biometricEnrollResult.postValue(Result.Error(result.message))
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

    fun logout() {
        viewModelScope.launch {
            securePrefs.edit()
                .remove("auth_token")
                .apply()

            userRepository.clearLocalData()

            _authState.postValue(AuthState.LoggedOut)
        }
    }
}
