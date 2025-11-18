package com.danielrothmann.randomuser.di

import androidx.room.Room
import com.danielrothmann.randomuser.data.local.AppDatabase
import com.danielrothmann.randomuser.data.remote.api.RandomUserApi
import com.danielrothmann.randomuser.data.repository.UserRepositoryImpl
import com.danielrothmann.randomuser.domain.repository.UserRepository
import com.danielrothmann.randomuser.domain.usecase.GetCachedUsersUseCase
import com.danielrothmann.randomuser.domain.usecase.GetRandomUsersUseCase
import com.danielrothmann.randomuser.domain.usecase.GetUserByIdUseCase
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
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "random_user_db"
        ).build()
    }

    single { get<AppDatabase>().userDao() }

    // Network
    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://randomuser.me/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(RandomUserApi::class.java)
    }

    // Repository
    single<UserRepository> {
        UserRepositoryImpl(get(), get())
    }

    // Use Cases
    factory { GetRandomUsersUseCase(get()) }
    factory { GetCachedUsersUseCase(get()) }
    factory { GetUserByIdUseCase(get()) }

    // ViewModels
    viewModel { MainViewModel(get(), get()) }
    viewModel { ListUsersViewModel(get(), get()) }
    viewModel { (userId: String) -> DetailsViewModel(userId, get()) }
}