package com.example.mybookslibrary.ui.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybookslibrary.R
import com.example.mybookslibrary.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiText {
    data class Resource(
        @StringRes val id: Int,
    ) : UiText()

    data class Dynamic(
        val value: String,
    ) : UiText()

    @Composable
    fun asString(): String =
        when (this) {
            is Resource -> stringResource(id)
            is Dynamic -> value
        }
}

private fun authError(message: String?): UiText =
    when (message) {
        "Username already exists" -> UiText.Resource(R.string.auth_error_username_exists)
        "Invalid username or password" -> UiText.Resource(R.string.auth_error_invalid_credentials)
        "Failed to save Google user" -> UiText.Resource(R.string.auth_error_google_save_failed)
        null -> UiText.Dynamic("")
        else -> UiText.Dynamic(message)
    }

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
        val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

        fun login(
            username: String,
            password: String,
        ) {
            if (_uiState.value is AuthState.Loading) return
            if (username.isBlank() || password.isBlank()) {
                _uiState.value = AuthState.Error(UiText.Resource(R.string.auth_error_empty_fields))
                return
            }
            _uiState.value = AuthState.Loading
            viewModelScope.launch {
                val result = authRepository.login(username, password)
                _uiState.value =
                    if (result.isSuccess) {
                        AuthState.Success
                    } else {
                        AuthState.Error(authError(result.exceptionOrNull()?.message))
                    }
            }
        }

        fun register(
            username: String,
            password: String,
        ) {
            if (_uiState.value is AuthState.Loading) return
            if (username.isBlank() || password.isBlank()) {
                _uiState.value = AuthState.Error(UiText.Resource(R.string.auth_error_empty_fields))
                return
            }
            _uiState.value = AuthState.Loading
            viewModelScope.launch {
                val result = authRepository.register(username, password)
                _uiState.value =
                    if (result.isSuccess) {
                        AuthState.Success
                    } else {
                        AuthState.Error(authError(result.exceptionOrNull()?.message))
                    }
            }
        }

        fun googleSignIn(context: Context) {
            if (_uiState.value is AuthState.Loading) return
            _uiState.value = AuthState.Loading
            viewModelScope.launch {
                val result = authRepository.googleSignIn(context)
                _uiState.value =
                    if (result.isSuccess) {
                        AuthState.Success
                    } else {
                        AuthState.Error(authError(result.exceptionOrNull()?.message))
                    }
            }
        }

        fun resetState() {
            _uiState.value = AuthState.Idle
        }
    }

sealed class AuthState {
    object Idle : AuthState()

    object Loading : AuthState()

    object Success : AuthState()

    data class Error(
        val message: UiText,
    ) : AuthState()
}
