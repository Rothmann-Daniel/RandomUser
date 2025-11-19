package com.danielrothmann.randomuser.domain.usecase

import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.repository.UserRepository

class SaveUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User) {
        repository.saveUser(user)
    }
}