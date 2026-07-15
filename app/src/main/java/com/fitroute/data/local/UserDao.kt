package com.fitroute.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // Ambil data user berdasarkan ID
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUser(id: String): Flow<UserEntity?>

    // Simpan user baru
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Update data profil
    @Update
    suspend fun updateUser(user: UserEntity)

    // Hapus user
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)
}