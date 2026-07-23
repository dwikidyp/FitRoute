package com.fitroute.data.repository

import com.fitroute.data.remote.AuthApiService
import com.fitroute.data.remote.BiometricEnrollRequest
import com.fitroute.data.remote.LoginRequest
import com.fitroute.data.remote.RegisterRequest
import com.fitroute.ui.auth.AuthResponse
import com.fitroute.ui.auth.Result

class AuthRepository(private val apiService: AuthApiService) {

    // Login
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            Result.Success(AuthResponse(token = response.access_token, userId = response.user.id))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Login gagal")
        }
    }

    // Register
    suspend fun register(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.register(RegisterRequest(email, password))
            Result.Success(AuthResponse(token = response.access_token, userId = response.user.id))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Registrasi gagal")
        }
    }

    // Enroll public key biometric ke server
    suspend fun enrollBiometric(publicKey: String, deviceUid: String): Result<Boolean> {
        return try {
            val response = apiService.enrollBiometric(
                BiometricEnrollRequest(
                    public_key = publicKey,
                    device_uid = deviceUid
                )
            )
            Result.Success(response.success)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Enroll biometric gagal")
        }
    }
}
