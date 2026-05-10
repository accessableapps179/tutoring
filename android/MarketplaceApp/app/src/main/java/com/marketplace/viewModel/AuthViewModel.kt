// app/src/main/java/com/marketplace/viewmodel/AuthViewModel.kt
package com.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.api.RetrofitClient
import com.marketplace.repository.AuthRepository
import com.marketplace.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isRegistered: Boolean = false,
    val token: String = "",
    val userId: String = "",
    val email: String = "",
    val role: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggingOut: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val messageRepository = MessageRepository()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private val _unreadCountOnLogin = MutableStateFlow(0L)
    val unreadCountOnLogin: StateFlow<Long> = _unreadCountOnLogin

    private val _logoutComplete = MutableStateFlow(false)
    val logoutComplete: StateFlow<Boolean> = _logoutComplete

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = repository.login(email, password)

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                RetrofitClient.setToken(response.token)
                saveFcmToken()
                checkUnreadOnLogin()
                _authState.value = AuthState(
                    isLoggedIn = true,
                    token      = response.token,
                    userId     = response.userId,
                    email      = response.email,
                    role       = response.role,
                    name       = response.name,
                    isLoading  = false,
                    errorMessage = null
                )
            } else {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Invalid email or password"
                )
            }
        }
    }

    fun register(email: String, password: String, role: String, name: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = repository.register(email, password, role, name)

            if (result.isSuccess) {
                val loginResult = repository.login(email, password)
                if (loginResult.isSuccess) {
                    val response = loginResult.getOrNull()!!
                    RetrofitClient.setToken(response.token)
                    saveFcmToken()
                    _authState.value = AuthState(
                        isLoggedIn   = true,
                        isRegistered = true,
                        token        = response.token,
                        userId       = response.userId,
                        email        = response.email,
                        role         = response.role,
                        name         = response.name,
                        isLoading    = false,
                        errorMessage = null
                    )
                } else {
                    _authState.value = _authState.value.copy(
                        isRegistered = true,
                        isLoading    = false,
                        errorMessage = null
                    )
                }
            } else {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Registration failed. Email may already be in use."
                )
            }
        }
    }

    private fun checkUnreadOnLogin() {
        viewModelScope.launch {
            val result = messageRepository.getUnreadCount()
            if (result.isSuccess) {
                _unreadCountOnLogin.value = result.getOrNull() ?: 0L
            }
        }
    }

    // Fix #7: the original wrapped addOnSuccessListener inside viewModelScope.launch,
    // which is unnecessary — addOnSuccessListener is a callback, not a suspend function.
    // The outer launch was redundant and added a spurious coroutine allocation on every login.
    // The inner launch (to call the suspend repository.saveFcmToken) is still needed.
    private fun saveFcmToken() {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    viewModelScope.launch {
                        repository.saveFcmToken(token)
                    }
                }
        } catch (e: Exception) {
            // FCM token save failed silently — user still logs in successfully
        }
    }

    fun deleteFcmTokenAndLogout() {
        viewModelScope.launch {
            try {
                repository.logout()
            } catch (e: Exception) {
                // silent
            }
            RetrofitClient.setToken("")
            _authState.value = AuthState()
            _unreadCountOnLogin.value = 0L
            _logoutComplete.value = true
        }
    }

    fun resetLogoutComplete() {
        _logoutComplete.value = false
    }

    fun resetRegistered() {
        _authState.value = _authState.value.copy(isRegistered = false)
    }
}