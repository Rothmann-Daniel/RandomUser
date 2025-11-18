package com.danielrothmann.randomuser.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.repository.UserRepository
import com.danielrothmann.randomuser.domain.usecase.GetCachedUsersUseCase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ListUsersViewModel(
    private val getCachedUsersUseCase: GetCachedUsersUseCase,
    private val repository: UserRepository
) : ViewModel() {

    val users = getCachedUsersUseCase().asLiveData()

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }
}