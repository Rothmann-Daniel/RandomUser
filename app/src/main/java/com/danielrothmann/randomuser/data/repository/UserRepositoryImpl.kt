package com.danielrothmann.randomuser.data.repository

import com.danielrothmann.randomuser.data.local.dao.UserDao
import com.danielrothmann.randomuser.data.mapper.toDomain
import com.danielrothmann.randomuser.data.mapper.toEntity
import com.danielrothmann.randomuser.data.remote.api.RandomUserApi
import com.danielrothmann.randomuser.domain.model.Resource
import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class UserRepositoryImpl(
    private val api: RandomUserApi,
    private val userDao: UserDao
) : UserRepository {

    override suspend fun fetchRandomUsers(
        results: Int,
        gender: String?,
        nationality: String?
    ): Resource<List<User>> {
        return try {
            val response = api.getRandomUsers(results, gender, nationality)
            if (response.isSuccessful) {
                val users = response.body()?.results?.map { it.toDomain() } ?: emptyList()

                // Сохраняем пользователей в базу данных
                userDao.insertUsers(users.map { it.toEntity() })

                Resource.Success(users)
            } else {
                Resource.Error("HTTP error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("HTTP exception: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.message}")
        }
    }

    override fun getCachedUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { users ->
            users.map { it.toDomain() }
        }
    }

    override suspend fun getUserById(uuid: String): User? {
        return userDao.getUserById(uuid)?.toDomain()
    }

    override suspend fun saveUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.toEntity())
    }
}