package com.marketplace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.viewmodel.PaymentCardViewModel

fun detectCardType(number: String): String {
    val clean = number.replace(" ", "")
    return when {
        clean.startsWith("4") -> "Visa"
        clean.startsWith("51") || clean.startsWith("52") || clean.startsWith("53") ||
                clean.startsWith("54") || clean.startsWith("55") ||
                (clean.length >= 6 && clean.substring(0, 6).toIntOrNull() in 222100..272099) -> "Mastercard"
        clean.startsWith("34") || clean.startsWith("37") -> "Amex"
        clean.startsWith("6011") || clean.startsWith("65") ||
                (clean.length >= 6 && clean.substring(0, 6).toIntOrNull() in 644000..649999) -> "Discover"
        clean.startsWith("3528") || clean.startsWith("3589") ||
                (clean.length >= 4 && clean.substring(0, 4).toIntOrNull() in 3528..3589) -> "JCB"
        clean.startsWith("62") || clean.startsWith("81") -> "UnionPay"
        clean.startsWith("36") || clean.startsWith("38") ||
                clean.startsWith("300") || clean.startsWith("301") ||
                clean.startsWith("302") || clean.startsWith("303") ||
                clean.startsWith("304") || clean.startsWith("305") -> "Diners Club"
        clean.startsWith("5018") || clean.startsWith("5020") ||
                clean.startsWith("5038") || clean.startsWith("6304") -> "Maestro"
        clean.startsWith("6304") || clean.startsWith("6759") ||
                clean.startsWith("6761") || clean.startsWith("6762") ||
                clean.startsWith("6763") -> "Maestro"
        clean.startsWith("2221") || clean.startsWith("2720") ||
                (clean.length >= 4 && clean.substring(0, 4).toIntOrNull() in 2221..2720) -> "Mastercard"
        else -> "Unknown"
    }
}

fun formatCardNumber(input: String): String {
    val clean = input.replace(" ", "").filter { it.isDigit() }
    val limited = clean.take(19)
    return limited.chunked(4).joinToString(" ")
}

fun cardTypeEmoji(cardType: String): String {
    return when (cardType) {
        "Visa" -> "💳 Visa"
        "Mastercard" -> "💳 Mastercard"
        "Amex" -> "💳 Amex"
        "Discover" -> "💳 Discover"
        "JCB" -> "💳 JCB"
        "UnionPay" -> "💳 UnionPay"
        "Diners Club" -> "💳 Diners Club"
        "Maestro" -> "💳 Maestro"
        else -> "💳 Card"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCardScreen(
    onBackClick: () -> Unit,
    viewModel: PaymentCardViewModel = viewModel()
) {
    val card by viewModel.card.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var cardholderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var backClicked by remember { mutableStateOf(false) }

    val cardType = detectCardType(cardNumber)

    LaunchedEffect(Unit) {
        viewModel.loadCard()
    }

    LaunchedEffect(card) {
        if (card != null) {
            cardholderName = card!!.cardholderName
            cardNumber = card!!.cardNumber
            expiryMonth = card!!.expiryMonth
            expiryYear = card!!.expiryYear
            cvv = card!!.cvv
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveSuccess()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Payment Method",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!backClicked) {
                                backClicked = true
                                onBackClick()
                            }
                        }
                    ) {
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
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Saved Payment Method",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Card type indicator
                    if (cardNumber.isNotBlank() && cardType != "Unknown") {
                        Text(
                            text = cardTypeEmoji(cardType),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = cardholderName,
                        onValueChange = { cardholderName = it },
                        label = { Text("Cardholder Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = {
                            cardNumber = formatCardNumber(it)
                        },
                        label = { Text("Card Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            if (cardType != "Unknown" && cardNumber.isNotBlank()) {
                                Text(
                                    text = cardType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = expiryMonth,
                            onValueChange = {
                                if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                                    expiryMonth = it
                                }
                            },
                            label = { Text("MM") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = expiryYear,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    expiryYear = it
                                }
                            },
                            label = { Text("YYYY") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = cvv,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    cvv = it
                                }
                            },
                            label = { Text("CVV") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PasswordVisualTransformation()
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.saveCard(
                                cardholderName = cardholderName,
                                cardNumber = cardNumber,
                                expiryMonth = expiryMonth,
                                expiryYear = expiryYear,
                                cvv = cvv,
                                cardType = cardType
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = cardholderName.isNotBlank() &&
                                cardNumber.isNotBlank() &&
                                expiryMonth.isNotBlank() &&
                                expiryYear.isNotBlank() &&
                                cvv.isNotBlank()
                    ) {
                        Text(
                            text = "Save Payment Method",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}