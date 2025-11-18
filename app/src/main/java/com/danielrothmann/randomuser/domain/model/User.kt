package com.danielrothmann.randomuser.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class User(
    val uuid: String,
    val gender: String,
    val fullName: String,
    val email: String,
    val username: String,
    val phone: String,
    val cell: String,
    val address: String,
    val city: String,
    val state: String,
    val country: String,
    val postcode: String,
    val coordinates: Coordinates,
    val age: Int,
    val registeredDate: String,
    val nationality: String,
    val pictureUrl: String
) : Parcelable

@Parcelize
data class Coordinates(
    val latitude: String,
    val longitude: String
) : Parcelable