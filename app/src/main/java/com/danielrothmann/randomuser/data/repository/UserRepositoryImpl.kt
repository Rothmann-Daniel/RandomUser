// data/repository/UserRepositoryImpl.kt
package com.danielrothmann.randomuser.data.repository

import android.util.Log
import com.danielrothmann.randomuser.data.local.dao.UserDao
import com.danielrothmann.randomuser.data.mapper.toDomain
import com.danielrothmann.randomuser.data.mapper.toEntity
import com.danielrothmann.randomuser.data.remote.api.RandomUserApi
import com.danielrothmann.randomuser.domain.model.Resource
import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response

class UserRepositoryImpl(
    private val api: RandomUserApi,
    private val userDao: UserDao
) : UserRepository {

    companion object {
        private const val TAG = "UserRepositoryImpl"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    override suspend fun fetchRandomUsers(
        results: Int,
        gender: String?,
        nationality: String?
    ): Resource<List<User>> {
        Log.d(TAG, "Starting fetch: results=$results, gender=$gender, nat=$nationality")

        // Стратегия 1: Попробовать с полными параметрами
        var result = tryFetchWithParams(results, gender, nationality)
        if (result is Resource.Success) return result

        // Стратегия 2: Если ошибка 500 и есть nationality, попробовать без него
        if (nationality != null && result is Resource.Error) {
            Log.d(TAG, "Retrying without nationality parameter...")
            result = tryFetchWithParams(results, gender, null)
            if (result is Resource.Success) return result
        }

        // Стратегия 3: Попробовать только с gender
        if (gender != null && result is Resource.Error) {
            Log.d(TAG, "Retrying with only gender parameter...")
            result = tryFetchWithParams(results, gender, null)
            if (result is Resource.Success) return result
        }

        // Стратегия 4: Попробовать вообще без параметров
        if (result is Resource.Error) {
            Log.d(TAG, "Retrying without any optional parameters...")
            result = tryFetchWithParams(results, null, null)
            if (result is Resource.Success) return result
        }

        // Стратегия 5: Вернуть кешированные данные
        Log.d(TAG, "All strategies failed, attempting to return cached data...")
        return tryReturnCachedData(result as? Resource.Error)
    }

    private suspend fun tryFetchWithParams(
        results: Int,
        gender: String?,
        nationality: String?
    ): Resource<List<User>> {
        repeat(MAX_RETRIES) { attempt ->
            try {
                Log.d(TAG, "Attempt ${attempt + 1}/$MAX_RETRIES: results=$results, gender=$gender, nat=$nationality")

                val response = api.getRandomUsers(results, gender, nationality)

                Log.d(TAG, "Response code: ${response.code()}, Success: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    return handleSuccessResponse(response)
                } else {
                    Log.e(TAG, "HTTP ${response.code()}: ${response.message()}")
                    logResponseDetails(response)

                    // Для ошибок 5xx не пытаемся повторять - это проблема сервера
                    if (response.code() >= 500) {
                        Log.e(TAG, "Server error ${response.code()}, stopping retries for this strategy")
                        return Resource.Error("Server error: HTTP ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error on attempt ${attempt + 1}", e)
                if (attempt == MAX_RETRIES - 1) {
                    return Resource.Error("Network error: ${e.localizedMessage}")
                }
            }

            if (attempt < MAX_RETRIES - 1) {
                delay(RETRY_DELAY_MS * (attempt + 1)) // Экспоненциальная задержка
            }
        }

        return Resource.Error("Failed after $MAX_RETRIES attempts")
    }

    private suspend fun handleSuccessResponse(response: Response<com.danielrothmann.randomuser.data.remote.dto.ApiResponse>): Resource<List<User>> {
        val body = response.body()

        if (body == null || body.results.isEmpty()) {
            Log.e(TAG, "Empty response body or no results")
            return Resource.Error("No data received from server")
        }

        try {
            val users = body.results.map { it.toDomain() }
            Log.d(TAG, "Successfully parsed ${users.size} users")

            // Сохраняем в БД
            userDao.insertUsers(users.map { it.toEntity() })
            Log.d(TAG, "Users saved to database")

            return Resource.Success(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing response", e)
            return Resource.Error("Error parsing data: ${e.localizedMessage}")
        }
    }

    private fun logResponseDetails(response: Response<*>) {
        try {
            val errorBody = response.errorBody()?.string()
            Log.e(TAG, "Error body: $errorBody")
        } catch (e: Exception) {
            Log.e(TAG, "Could not read error body", e)
        }
    }

    private suspend fun tryReturnCachedData(error: Resource.Error?): Resource<List<User>> {
        return try {
            val cachedUsers = userDao.getAllUsersSync()

            if (cachedUsers.isNotEmpty()) {
                Log.d(TAG, "Returning ${cachedUsers.size} cached users")
                Resource.Success(cachedUsers.map { it.toDomain() })
            } else {
                Log.e(TAG, "No cached data available")
                error ?: Resource.Error("Unable to fetch data and no cached data available")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing cached data", e)
            error ?: Resource.Error("Network error and unable to access cached data")
        }
    }

    override fun getCachedUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
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