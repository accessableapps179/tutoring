// Android Studio
// app/src/main/java/com/marketplace/ui/screens/TeacherBalanceScreen.kt
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.dto.LedgerEntryDto
import com.marketplace.viewmodel.LedgerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherBalanceScreen(
    onBackClick: () -> Unit,
    ledgerViewModel: LedgerViewModel = viewModel()
) {
    val ledger by ledgerViewModel.ledger.collectAsState()
    val isLoading by ledgerViewModel.isLoading.collectAsState()
    val errorMessage by ledgerViewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        ledgerViewModel.loadLedger()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Balance", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                errorMessage != null -> Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                ledger != null -> {
                    val data = ledger!!
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Available Balance",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "£${"%.2f".format(data.balance)}",
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = "Will be paid out when merchant account connected",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Trial History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (data.transactions.isEmpty()) {
                            item {
                                Text(
                                    text = "No trials yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            items(data.transactions) { tx ->
                                TeacherTransactionRow(tx)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherTransactionRow(tx: LedgerEntryDto) {
    var expanded by remember { mutableStateOf(false) }

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
                    Text(text = if (tx.happy) "✅" else "❌", fontSize = 18.sp)
                    Column {
                        Text(
                            text = if (tx.happy) "Trial lesson — Happy" else "Trial lesson — Not happy",
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
                        text = if (tx.happy) "+£${"%.2f".format(tx.amount)}" else "£0.00",
                        fontWeight = FontWeight.Bold,
                        color = if (tx.happy) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline,
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
                    LedgerDetailRow(label = "Student",     value = tx.studentName)
                    LedgerDetailRow(label = "Lesson date", value = tx.slotDate)
                    LedgerDetailRow(
                        label = "Recorded",
                        value = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
                            .format(Date(tx.timestamp))
                    )
                    LedgerDetailRow(label = "Booking ref", value = tx.bookingId)
                    LedgerDetailRow(
                        label = "Amount credited",
                        value = if (tx.happy) "£${"%.2f".format(tx.amount)}" else "£0.00 — student was not happy"
                    )
                }
            }
        }
    }
}