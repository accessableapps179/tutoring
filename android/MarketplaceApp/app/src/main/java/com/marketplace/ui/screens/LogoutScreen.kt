package com.marketplace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marketplace.MarketplaceFirebaseService
import com.marketplace.viewmodel.AuthViewModel
import com.marketplace.viewmodel.MessageViewModel

@Composable
fun LogoutScreen(
    authViewModel: AuthViewModel,
    messageViewModel: MessageViewModel,
    onLogoutComplete: () -> Unit,
    onCancelLogout: () -> Unit
) {
    val logoutComplete by authViewModel.logoutComplete.collectAsState()
    var isLoggingOut by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(logoutComplete) {
        if (logoutComplete) {
            authViewModel.resetLogoutComplete()
            onLogoutComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoggingOut) {
            CircularProgressIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Are you sure you want to log out?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelLogout,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "No, go back",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            isLoggingOut = true
                            messageViewModel.resetUnreadCount()
                            MarketplaceFirebaseService.clearAllNotifications(context)
                            authViewModel.deleteFcmTokenAndLogout()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "Yes, log out",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}