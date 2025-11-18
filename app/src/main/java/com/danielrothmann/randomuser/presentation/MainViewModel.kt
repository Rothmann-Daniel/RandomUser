package com.danielrothmann.randomuser.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielrothmann.randomuser.domain.model.Resource
import com.danielrothmann.randomuser.domain.model.User
import com.danielrothmann.randomuser.domain.usecase.GetRandomUsersUseCase
import com.danielrothmann.randomuser.domain.usecase.GetCachedUsersUseCase
import kotlinx.coroutines.launch

class MainViewModel(
    private val getRandomUsersUseCase: GetRandomUsersUseCase,
    private val getCachedUsersUseCase: GetCachedUsersUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _usersState = MutableLiveData<Resource<List<User>>>()
    val usersState: LiveData<Resource<List<User>>> = _usersState

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun generateUser(gender: String?, nationality: String?) {
        viewModelScope.launch {
            _usersState.value = Resource.Loading

            val genderParam = when (gender?.lowercase()) {
                "male" -> "male"
                "female" -> "female"
                else -> null
            }

            Log.d(TAG, "Generating user: gender=$genderParam, nationality=$nationality")

            val result = getRandomUsersUseCase(
                results = 1,
                gender = genderParam,
                nationality = nationality
            )

            _usersState.value = result

            if (result is Resource.Error) {
                _errorMessage.value = buildErrorMessage(result.message, nationality)
            }
        }
    }

    private fun buildErrorMessage(originalMessage: String, nationality: String?): String {
        return when {
            originalMessage.contains("500") && nationality != null -> {
                "The API is having issues with nationality '$nationality'. Try without selecting nationality or choose a different one."
            }
            originalMessage.contains("500") -> {
                "The Random User API is temporarily unavailable. Please try again later."
            }
            originalMessage.contains("Network") || originalMessage.contains("network") -> {
                "No internet connection. Please check your network and try again."
            }
            else -> originalMessage
        }
    }
}