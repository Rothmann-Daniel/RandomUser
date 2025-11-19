package com.danielrothmann.randomuser.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielrothmann.randomuser.domain.model.Resource
import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.usecase.GetCachedUsersUseCase
import com.danielrothmann.randomuser.domain.usecase.GetRandomUsersUseCase
import com.danielrothmann.randomuser.domain.usecase.SaveUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(
    private val getRandomUsersUseCase: GetRandomUsersUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val getCachedUsersUseCase: GetCachedUsersUseCase
) : ViewModel() {

    // Изменил тип на nullable для возможности сброса
    private val _generatedUserState = MutableStateFlow<Resource<User?>?>(null)
    val generatedUserState: StateFlow<Resource<User?>?> = _generatedUserState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val hasUsers = getCachedUsersUseCase()
        .map { it.isNotEmpty() }

    // Сброс состояния генерации пользователя
    fun clearGeneratedUserState() {
        _generatedUserState.value = null
    }

    fun generateSingleUser(gender: String?, nationality: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _generatedUserState.value = Resource.Loading

            val result = getRandomUsersUseCase(1, gender, nationality)

            when (result) {
                is Resource.Success -> {
                    if (result.data.isNotEmpty()) {
                        val user = result.data.first()
                        // Сохраняем пользователя в базу данных
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