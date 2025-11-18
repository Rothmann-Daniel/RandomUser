package com.danielrothmann.randomuser

import android.app.Application
import com.danielrothmann.randomuser.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class RandomUserApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@RandomUserApplication)
            modules(appModule)
        }
    }
}