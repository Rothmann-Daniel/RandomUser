package com.danielrothmann.randomuser.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.danielrothmann.randomuser.domain.usecase.GetUserByIdUseCase

class DetailsViewModelFactory(
    private val uuid: String,
    private val getUserByIdUseCase: GetUserByIdUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            return DetailsViewModel(uuid, getUserByIdUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}