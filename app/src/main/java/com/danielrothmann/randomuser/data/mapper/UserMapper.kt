package com.danielrothmann.randomuser.data.mapper

import com.danielrothmann.randomuser.data.local.entity.UserEntity
import com.danielrothmann.randomuser.data.remote.dto.UserDto
import com.danielrothmann.randomuser.domain.model.Coordinates
import com.danielrothmann.randomuser.domain.model.User

fun UserDto.toDomain(): User {
    return User(
        uuid = login.uuid,
        gender = gender,
        fullName = "${name.title} ${name.first} ${name.last}",
        email = email,
        username = login.username,
        phone = phone,
        cell = cell,
        address = "${location.street.number} ${location.street.name}",
        city = location.city,
        state = location.state,
        country = location.country,
        postcode = when (val pc = location.postcode) {
            is String -> pc
            is Number -> pc.toString()
            else -> pc.toString()
        },
        coordinates = Coordinates(
            latitude = location.coordinates.latitude,
            longitude = location.coordinates.longitude
        ),
        age = dob.age,
        registeredDate = registered.date,
        nationality = nat,
        pictureUrl = picture.large
    )
}

fun User.toEntity(): UserEntity {
    val nameParts = fullName.split(" ")
    val addressParts = address.split(" ", limit = 2)

    return UserEntity(
        uuid = uuid,
        gender = gender,
        title = nameParts.getOrNull(0) ?: "",
        firstName = nameParts.getOrNull(1) ?: "",
        lastName = nameParts.drop(2).joinToString(" "),
        email = email,
        username = username,
        phone = phone,
        cell = cell,
        streetNumber = addressParts.getOrNull(0)?.toIntOrNull() ?: 0,
        streetName = addressParts.getOrNull(1) ?: address,
        city = city,
        state = state,
        country = country,
        postcode = postcode,
        latitude = coordinates.latitude,
        longitude = coordinates.longitude,
        age = age,
        registeredDate = registeredDate,
        nationality = nationality,
        pictureUrl = pictureUrl
    )
}

fun UserEntity.toDomain(): User {
    return User(
        uuid = uuid,
        gender = gender,
        fullName = "$title $firstName $lastName".trim(),
        email = email,
        username = username,
        phone = phone,
        cell = cell,
        address = if (streetNumber > 0) "$streetNumber $streetName" else streetName,
        city = city,
        state = state,
        country = country,
        postcode = postcode,
        coordinates = Coordinates(
            latitude = latitude,
            longitude = longitude
        ),
        age = age,
        registeredDate = registeredDate,
        nationality = nationality,
        pictureUrl = pictureUrl
    )
}