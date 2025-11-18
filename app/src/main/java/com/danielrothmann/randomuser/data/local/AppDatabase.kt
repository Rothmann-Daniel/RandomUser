package com.danielrothmann.randomuser.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.danielrothmann.randomuser.data.local.dao.UserDao
import com.danielrothmann.randomuser.data.local.entity.UserEntity


@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

// init dev