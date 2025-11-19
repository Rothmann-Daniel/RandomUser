package com.danielrothmann.randomuser.domain.usecase

import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.repository.UserRepository

class DeleteUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User) {
        repository.deleteUser(user)
    }
}