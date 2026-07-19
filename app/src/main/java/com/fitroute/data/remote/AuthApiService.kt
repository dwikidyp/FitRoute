package com.fitroute.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

// Request
data class LoginRequest(
    val email: String,
    val password: String
)

// Request Register
data class RegisterRequest(
    val email: String,
    val password: String
)

// Response Supabase Auth
data class SupabaseAuthResponse(
    val access_token: String,
    val token_type: String,
    val user: SupabaseUser
)

data class SupabaseUser(
    val id: String,
    val email: String
)

interface AuthApiService {

    // Login dengan email & password
    @POST("token?grant_type=password")
    suspend fun login(@Body request: LoginRequest): SupabaseAuthResponse

    // Register akun baru
    @POST("signup")
    suspend fun register(@Body request: RegisterRequest): SupabaseAuthResponse

    // Enroll public key biometric
    @POST("rest/v1/biometric_keys")
    suspend fun enrollBiometric(@Body request: BiometricEnrollRequest): BiometricEnrollResponse

    // Upload sesi ke Supabase
    @POST("rest/v1/workout_sessions")
    suspend fun uploadSessions(
        @Body sessions: List<SessionDto>
    ): retrofit2.Response<Unit>
}

data class BiometricEnrollRequest(
    val public_key: String,
    val device_uid: String
)

data class BiometricEnrollResponse(
    val success: Boolean,
    val message: String
)