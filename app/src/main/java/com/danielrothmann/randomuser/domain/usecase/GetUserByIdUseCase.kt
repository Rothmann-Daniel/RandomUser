package com.danielrothmann.randomuser.domain.usecase

import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.repository.UserRepository

class GetUserByIdUseCase (
    private val repository: UserRepository
) {
    suspend operator fun invoke(uuid: String): User? {
        return repository.getUserById(uuid)
    }
}