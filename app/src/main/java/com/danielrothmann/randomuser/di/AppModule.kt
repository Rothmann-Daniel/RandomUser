package com.danielrothmann.randomuser.di

import android.content.Context
import androidx.room.Room
import com.danielrothmann.randomuser.data.local.AppDatabase
import com.danielrothmann.randomuser.data.remote.api.RandomUserApi
import com.danielrothmann.randomuser.data.repository.UserRepositoryImpl
import com.danielrothmann.randomuser.domain.repository.UserRepository
import com.danielrothmann.randomuser.domain.usecase.*
import com.danielrothmann.randomuser.presentation.DetailsViewModel
import com.danielrothmann.randomuser.presentation.ListUsersViewModel
import com.danielrothmann.randomuser.presentation.MainViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    // Database
    single { provideDatabase(androidContext()) }
    single { get<AppDatabase>().userDao() }

    // Network
    single { provideOkHttpClient() }
    single { provideRetrofit(get()) }
    single { provideRandomUserApi(get()) }

    // Repository
    single<UserRepository> { UserRepositoryImpl(get(), get()) }

    // Use Cases
    factory { GetRandomUsersUseCase(get()) }
    factory { GetCachedUsersUseCase(get()) }
    factory { GetUserByIdUseCase(get()) }
    factory { SaveUserUseCase(get()) }
    factory { DeleteUserUseCase(get()) }

    // ViewModels
    viewModel { MainViewModel(get(), get()) }
    viewModel { ListUsersViewModel(get(), get()) } // Убрали лишний параметр
    viewModel { (uuid: String) -> DetailsViewModel(uuid, get()) }
}

private fun provideDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "random_user.db"
    ).build()
}

private fun provideOkHttpClient(): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}

private fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://randomuser.me/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

private fun provideRandomUserApi(retrofit: Retrofit): RandomUserApi {
    return retrofit.create(RandomUserApi::class.java)
}