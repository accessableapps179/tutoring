// Android Studio
// app/src/main/java/com/marketplace/ui/screens/ChangePasswordScreen.kt
package com.marketplace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChangePasswordViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.changePassword(currentPassword, newPassword)
            if (result.isSuccess) {
                _success.value = true
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to change password"
            }
            _isLoading.value = false
        }
    }

    fun resetSuccess() {
        _success.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    viewModel: ChangePasswordViewModel = viewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val success by viewModel.success.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var mismatch by remember { mutableStateOf(false) }

    if (success) {
        LaunchedEffect(Unit) {
            viewModel.resetSuccess()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Change Password",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (currentVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { currentVisible = !currentVisible }) {
                            Icon(
                                imageVector = if (currentVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (newVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { newVisible = !newVisible }) {
                            Icon(
                                imageVector = if (newVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = mismatch,
                    visualTransformation = if (confirmVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                imageVector = if (confirmVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    }
                )

                if (mismatch) {
                    Text(
                        text = "Passwords do not match",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Button(
                        onClick = {
                            if (newPassword != confirmPassword) {
                                mismatch = true
                            } else {
                                mismatch = false
                                viewModel.changePassword(currentPassword, newPassword)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentPassword.isNotBlank() &&
                                newPassword.isNotBlank() &&
                                confirmPassword.isNotBlank()
                    ) {
                        Text(
                            text = "Change Password",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
