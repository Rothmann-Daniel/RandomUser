package com.danielrothmann.randomuser.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielrothmann.randomuser.domain.model.Resource
import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.usecase.DeleteUserUseCase
import com.danielrothmann.randomuser.domain.usecase.GetCachedUsersUseCase
import com.danielrothmann.randomuser.domain.usecase.GetRandomUsersUseCase
import com.danielrothmann.randomuser.domain.usecase.SaveUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListUsersViewModel(
    private val getCachedUsersUseCase: GetCachedUsersUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val getRandomUsersUseCase: GetRandomUsersUseCase,
    private val saveUserUseCase: SaveUserUseCase
) : ViewModel() {

    val users = getCachedUsersUseCase()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _generatedUserState = MutableStateFlow<Resource<User?>>(Resource.Loading)
    val generatedUserState: StateFlow<Resource<User?>> = _generatedUserState

    fun deleteUser(user: User) {
        viewModelScope.launch {
            deleteUserUseCase(user)
        }
    }

    fun generateAndSaveUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _generatedUserState.value = Resource.Loading

            val result = getRandomUsersUseCase(1, null, null)

            when (result) {
                is Resource.Success -> {
                    if (result.data.isNotEmpty()) {
                        val user = result.data.first()
                        saveUserUseCase(user)
                        _generatedUserState.value = Resource.Success(user)
                    } else {
                        _generatedUserState.value = Resource.Error("No user generated")
                    }
                }
                is Resource.Error -> {
                    _generatedUserState.value = Resource.Error(result.message)
                }
                is Resource.Loading -> {
                    // Уже обрабатывается
                }
            }

            _isLoading.value = false
        }
    }
}