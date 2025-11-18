package com.danielrothmann.randomuser.data.remote.api

import com.danielrothmann.randomuser.data.remote.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RandomUserApi {

    // Основной метод с поддержкой всех параметров
    @GET("api/")
    suspend fun getRandomUsers(
        @Query("results") results: Int = 1,
        @Query("gender") gender: String? = null,
        @Query("nat") nationality: String? = null
    ): Response<ApiResponse>

    // Запасной метод без nationality для случаев ошибок
    @GET("api/")
    suspend fun getRandomUsersSimple(
        @Query("results") results: Int = 1,
        @Query("gender") gender: String? = null
    ): Response<ApiResponse>

    // Метод для получения пользователя по seed (для тестирования)
    @GET("api/")
    suspend fun getUserBySeed(
        @Query("seed") seed: String,
        @Query("results") results: Int = 1
    ): Response<ApiResponse>
}