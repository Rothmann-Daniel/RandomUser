package com.danielrothmann.randomuser.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uuid: String,
    val gender: String,
    val title: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val username: String,
    val phone: String,
    val cell: String,
    val streetNumber: Int,
    val streetName: String,
    val city: String,
    val state: String,
    val country: String,
    val postcode: String,
    val latitude: String,
    val longitude: String,
    val age: Int,
    val registeredDate: String,
    val nationality: String,
    val pictureUrl: String,
    val savedTimestamp: Long = System.currentTimeMillis()
)