package com.danielrothmann.randomuser.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.usecase.DeleteUserUseCase
import com.danielrothmann.randomuser.domain.usecase.GetCachedUsersUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListUsersViewModel(
    private val getCachedUsersUseCase: GetCachedUsersUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) : ViewModel() {

    val users: StateFlow<List<User>> = getCachedUsersUseCase()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteUser(user: User) {
        viewModelScope.launch {
            deleteUserUseCase(user)
        }
    }
}