package com.danielrothmann.randomuser.domain.usecase

import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class GetCachedUsersUseCase (
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<List<User>> {
        return repository.getCachedUsers()
    }
}
