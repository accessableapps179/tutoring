package com.marketplace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.viewmodel.PlatonicAvailabilityViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val PagBlue   = Color(0xFF1565C0)
private val PagGrey   = Color(0xFFBDBDBD)
private val PagSelect = Color(0xFF0D47A1)

private val DAY_NAMES = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatonicAvailabilityScreen(
    onBackClick: () -> Unit,
    viewModel: PlatonicAvailabilityViewModel = viewModel()
) {
    val slots      by viewModel.platonicSlots.collectAsState()
    val isLoading  by viewModel.isLoading.collectAsState()
    val stampResult by viewModel.stampResult.collectAsState()

    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) } // weekNumber to dayOfWeek

    val today = LocalDate.now()
    val stampYear  = today.year
    val stampMonth = today.monthValue
    val monthName  = today.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

    LaunchedEffect(Unit) { viewModel.load() }

    // Pre-build a lookup: (weekNumber, dayOfWeek) -> set of hours
    val slotMap: Map<Pair<Int, Int>, Set<Double>> = slots.groupBy { Pair(it.weekNumber, it.dayOfWeek) }
        .mapValues { entry -> entry.value.map { it.hour }.toSet() }

    if (stampResult != null) {
        val result = stampResult!!
        AlertDialog(
            onDismissRequest = { viewModel.clearStampResult() },
            title = { Text("Stamped to $monthName $stampYear", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${result.slotsWritten} slot(s) applied.")
                    if (result.conflicts.isNotEmpty()) {
                        Text(
                            "${result.conflicts.size} existing booking(s) were skipped — " +
                            "they are flagged on your calendar.",
                            color = Color(0xFFE65100)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearStampResult() }) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Availability Template", fontWeight = FontWeight.Bold) },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // ─── Grid header ──────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Spacer(modifier = Modifier.width(36.dp))
                    DAY_NAMES.forEach { name ->
                        Text(
                            text = name,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ─── Grid rows ────────────────────────────────────────
                (1..4).forEach { weekNum ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wk$weekNum",
                            modifier = Modifier.width(36.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        (1..7).forEach { dow ->
                            val key      = Pair(weekNum, dow)
                            val hasSlots = slotMap[key]?.isNotEmpty() == true
                            val isSelected = selectedCell == key

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> PagSelect
                                            hasSlots   -> PagBlue
                                            else       -> PagGrey
                                        }
                                    )
                                    .clickable {
                                        selectedCell = if (selectedCell == key) null else key
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasSlots && !isSelected) {
                                    val count = slotMap[key]!!.size
                                    Text(
                                        text = "$count",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // ─── Day drill-down ───────────────────────────────────
                selectedCell?.let { (weekNum, dow) ->
                    val dayName     = DAY_NAMES[dow - 1]
                    val selectedHours = slotMap[Pair(weekNum, dow)] ?: emptySet()

                    Text(
                        text = "Week $weekNum · $dayName",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // All 48 half-hour slots 0:00–23:30 in rows of 4
                    val allHours = (0 until 48).map { it * 0.5 }
                    allHours.chunked(4).forEach { rowHours ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowHours.forEach { h ->
                                val on = h in selectedHours
                                Box(
                                    modifier = Modifier
                                        .size(width = 72.dp, height = 40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (on) PagBlue else PagGrey)
                                        .clickable { viewModel.toggleSlot(weekNum, dow, h) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = formatSlotTime(h),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ─── Stamp button ─────────────────────────────────────
                Button(
                    onClick = { viewModel.stampMonth(stampYear, stampMonth) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Stamp to $monthName $stampYear")
                }

                Text(
                    text = "Stamping applies this template to every day of the month. " +
                           "Existing bookings are never changed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
