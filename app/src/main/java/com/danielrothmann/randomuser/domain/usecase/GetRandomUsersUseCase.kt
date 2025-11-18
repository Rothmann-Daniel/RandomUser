package com.danielrothmann.randomuser.domain.usecase

import com.danielrothmann.randomuser.domain.model.Resource
import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.repository.UserRepository


class GetRandomUsersUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        results: Int = 10,
        gender: String? = null,
        nationality: String? = null
    ): Resource<List<User>> {
        return repository.fetchRandomUsers(results, gender, nationality)
    }
}