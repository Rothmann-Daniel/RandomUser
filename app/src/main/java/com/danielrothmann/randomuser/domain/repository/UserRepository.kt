package com.danielrothmann.randomuser.domain.repository


import com.danielrothmann.randomuser.domain.model.Resource
import com.danielrothmann.randomuser.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun fetchRandomUsers(
        results: Int = 10,
        gender: String? = null,
        nationality: String? = null
    ): Resource<List<User>>

    fun getCachedUsers(): Flow<List<User>>

    suspend fun getUserById(uuid: String): User?

    suspend fun saveUser(user: User)

    suspend fun deleteUser(user: User)
}
