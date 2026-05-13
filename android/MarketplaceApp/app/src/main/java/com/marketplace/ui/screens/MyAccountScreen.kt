package com.marketplace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAccountScreen(
    role: String,
    onChangePasswordClick: () -> Unit,
    onPaymentCardClick: () -> Unit,
    onBankDetailsClick: () -> Unit,
    onMyBalanceClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Account", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onChangePasswordClick, modifier = Modifier.fillMaxWidth()) {
                Text("Change Password", fontWeight = FontWeight.Bold)
            }
            if (role == "STUDENT") {
                Button(onClick = onPaymentCardClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Payment Card", fontWeight = FontWeight.Bold)
                }
            }
            if (role == "TEACHER") {
                Button(onClick = onBankDetailsClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Bank Details", fontWeight = FontWeight.Bold)
                }
            }
            Button(onClick = onMyBalanceClick, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (role == "TEACHER") "💰 My Balance" else "My Balance",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
