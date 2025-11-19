
package com.danielrothmann.randomuser.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.usecase.GetUserByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val uuid: String,
    private val getUserByIdUseCase: GetUserByIdUseCase
) : ViewModel() {

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorState.value = null

            try {
                val user = getUserByIdUseCase(uuid)
                if (user != null) {
                    _userState.value = user
                } else {
                    _errorState.value = "User not found"
                }
            } catch (e: Exception) {
                _errorState.value = "Error loading user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}