package com.danielrothmann.randomuser.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielrothmann.randomuser.domain.model.Resource
import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.usecase.GetRandomUsersUseCase
import com.danielrothmann.randomuser.domain.usecase.SaveUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val getRandomUsersUseCase: GetRandomUsersUseCase,
    private val saveUserUseCase: SaveUserUseCase
) : ViewModel() {

    private val _usersState = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val usersState: StateFlow<Resource<List<User>>> = _usersState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun generateUsers(gender: String?, nationality: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _usersState.value = Resource.Loading

            val result = getRandomUsersUseCase(10, gender, nationality)
            _usersState.value = result

            // Сохраняем успешно полученных пользователей
            if (result is Resource.Success) {
                result.data.forEach { user ->
                    saveUserUseCase(user)
                }
            }

            _isLoading.value = false
        }
    }
}