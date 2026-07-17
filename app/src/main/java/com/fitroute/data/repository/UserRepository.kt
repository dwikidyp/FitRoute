package com.fitroute.data.repository

import com.fitroute.data.local.UserDao
import com.fitroute.data.local.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    // Ambil profil user
    fun getUser(userId: String): Flow<UserEntity?> {
        return userDao.getUser(userId)
    }

    // Simpan profil baru
    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    // Update profil
    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    // Hapus saat logout
    suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    // Hapus semua data user lokal
    suspend fun clearLocalData() {
        userDao.deleteAllUsers()
    }
}