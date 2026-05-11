// Android Studio
// app/src/main/java/com/marketplace/ui/screens/AdminScreen.kt
package com.marketplace.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.dto.LedgerEntryDto
import com.marketplace.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBackClick: () -> Unit,
    adminViewModel: AdminViewModel = viewModel()
) {
    val platformLedger  by adminViewModel.platformLedger.collectAsState()
    val commissionRate  by adminViewModel.commissionRate.collectAsState()
    val isLoading       by adminViewModel.isLoading.collectAsState()
    val errorMessage    by adminViewModel.errorMessage.collectAsState()
    val saveSuccess     by adminViewModel.saveSuccess.collectAsState()

    LaunchedEffect(Unit) {
        adminViewModel.loadPlatformLedger()
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) adminViewModel.clearSaveSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Admin Panel",
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
                    containerColor = Color(0xFF37474F),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                errorMessage != null -> Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                platformLedger != null -> {
                    val data = platformLedger!!
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        // Platform balance card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F)),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Platform Balance",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "£${"%.2f".format(data.balance)}",
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Commission collected from all trial lessons",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        // Commission rate card
                        item {
                            CommissionRateCard(
                                currentRate = commissionRate,
                                onSave      = { rate -> adminViewModel.updateCommissionRate(rate) }
                            )
                        }

                        item {
                            Text(
                                text = "Commission History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (data.transactions.isEmpty()) {
                            item {
                                Text(
                                    text = "No platform transactions yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            items(data.transactions) { tx ->
                                AdminTransactionRow(tx)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommissionRateCard(
    currentRate: Double?,
    onSave: (Double) -> Unit
) {
    var editing     by remember { mutableStateOf(false) }
    var inputValue  by remember(currentRate) { mutableStateOf(currentRate?.let { "%.1f".format(it) } ?: "") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Commission Rate",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (!editing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentRate != null) "${"%.1f".format(currentRate)}%" else "Loading…",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                    Button(onClick = { editing = true }) {
                        Text("Edit")
                    }
                }
                Text(
                    text = "Deducted from every lesson before crediting teacher",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text("Commission %") },
                    suffix = { Text("%") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val rate = inputValue.toDoubleOrNull()
                            if (rate != null && rate in 0.0..100.0) {
                                onSave(rate)
                                editing = false
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val rate = inputValue.toDoubleOrNull()
                            if (rate != null && rate in 0.0..100.0) {
                                onSave(rate)
                                editing = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                    Button(
                        onClick = {
                            inputValue = currentRate?.let { "%.1f".format(it) } ?: ""
                            editing = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTransactionRow(tx: LedgerEntryDto) {
    var expanded by remember { mutableStateOf(false) }

    val emoji = if (tx.happy) "✅" else "❌"
    val label = if (tx.happy) "Commission — happy trial" else "Commission — unhappy trial"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tx.slotDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "+£${"%.2f".format(tx.amount)}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        fontSize = 16.sp
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider()
                    LedgerDetailRow(label = "Student",        value = tx.studentName)
                    LedgerDetailRow(label = "Teacher",        value = tx.teacherName)
                    LedgerDetailRow(label = "Lesson date",    value = tx.slotDate)
                    LedgerDetailRow(
                        label = "Recorded",
                        value = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
                            .format(Date(tx.timestamp))
                    )
                    LedgerDetailRow(label = "Booking ref",    value = tx.bookingId)
                    if (tx.lessonAmount != null && tx.commissionPercent != null) {
                        LedgerDetailRow(
                            label = "Lesson amount",
                            value = "£${"%.2f".format(tx.lessonAmount)}"
                        )
                        LedgerDetailRow(
                            label = "Commission rate",
                            value = "${"%.1f".format(tx.commissionPercent)}%"
                        )
                        LedgerDetailRow(
                            label = "Platform earned",
                            value = "£${"%.2f".format(tx.amount)}  (${"%.1f".format(tx.commissionPercent)}% of £${"%.2f".format(tx.lessonAmount)})"
                        )
                    } else {
                        LedgerDetailRow(label = "Platform earned", value = "£${"%.2f".format(tx.amount)}")
                    }
                }
            }
        }
    }
}
