package com.marketplace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val CAL_DAY_HEADERS = listOf("M", "T", "W", "T", "F", "S", "S")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthCalendarScreen(
    teacherName: String,
    onBackClick: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }

    val firstDay    = displayMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.value - 1  // Mon=0 … Sun=6
    val daysInMonth = displayMonth.lengthOfMonth()
    val rows        = (startOffset + daysInMonth + 6) / 7

    val monthLabel = displayMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
            "  ${displayMonth.year}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book with $teacherName", fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // ─── Month navigation ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { displayMonth = displayMonth.minusMonths(1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
                }
                Text(text = monthLabel, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                IconButton(onClick = { displayMonth = displayMonth.plusMonths(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─── Day-of-week headers ──────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                CAL_DAY_HEADERS.forEach { d ->
                    Text(
                        text = d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }
            }

            // Top border
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Black))

            // ─── Calendar grid — fills remaining screen height ────────────────
            Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
            repeat(rows) { rowIdx ->
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    repeat(7) { colIdx ->
                        val dayNum    = rowIdx * 7 + colIdx - startOffset + 1
                        val validDay  = dayNum in 1..daysInMonth
                        val date      = if (validDay) displayMonth.atDay(dayNum) else null
                        val isPast    = date?.isBefore(today) == true
                        val isToday   = date == today

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(1.5.dp, Color(0xFFCCCCCC))
                                .then(
                                    if (validDay && !isPast)
                                        Modifier.clickable { onDateSelected(date!!) }
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (validDay) {
                                if (isToday) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(Color.Black)
                                    )
                                }
                                Text(
                                    text = dayNum.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = when {
                                        isToday -> Color.White
                                        isPast  -> Color(0xFFBBBBBB)
                                        else    -> Color.Black
                                    }
                                )
                            }
                        }
                    }
                }
            }
            } // end grid Column
        }
        } // end centering Box
    }
}
