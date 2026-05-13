// Android Studio
// app/src/main/java/com/marketplace/ui/screens/SlotBookingScreen.kt
package com.marketplace.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.Session
import com.marketplace.dto.AvailableSlotDto
import com.marketplace.viewmodel.AvailabilityViewModel
import com.marketplace.viewmodel.BookingViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private data class SlotPair(val first: AvailableSlotDto, val second: AvailableSlotDto)

private fun buildSlotPairs(slots: List<AvailableSlotDto>): List<SlotPair> {
    val sorted = slots.sortedBy { it.hour }
    val result = mutableListOf<SlotPair>()
    var i = 0
    while (i < sorted.size) {
        val cur = sorted[i]
        val nxt = sorted.getOrNull(i + 1)
        if (nxt != null && nxt.hour == cur.hour + 0.5 && !cur.isBooked && !nxt.isBooked) {
            result.add(SlotPair(cur, nxt))
            i += 2
        } else {
            i++
        }
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotBookingScreen(
    teacherId: String,
    teacherName: String,
    studentName: String,
    onBackClick: () -> Unit,
    onBookingSuccess: () -> Unit,
    availabilityViewModel: AvailabilityViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel()
) {
    val availableSlots by availabilityViewModel.availableSlots.collectAsState()
    val selectedSlot by availabilityViewModel.selectedSlot.collectAsState()
    val isLoading by availabilityViewModel.isLoading.collectAsState()
    val bookingSuccess by bookingViewModel.bookingSuccess.collectAsState()
    val isBookingLoading by bookingViewModel.isLoading.collectAsState()

    val isPostTrial = Session.pendingContactId.isNotEmpty()
    var selectedDuration by remember { mutableStateOf(1) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var weekOffset by remember { mutableStateOf(0) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()

    fun getWeekStart(offset: Int): LocalDate {
        val monday = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        return monday.plusWeeks(offset.toLong())
    }

    val weekStart = getWeekStart(weekOffset)
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }

    LaunchedEffect(Unit) {
        availabilityViewModel.clearSelectedSlot()
        availabilityViewModel.loadHourRange(teacherId)
        availabilityViewModel.loadSlotsForDate(teacherId, selectedDate.format(dateFormatter))
    }

    LaunchedEffect(selectedDate, weekOffset) {
        availabilityViewModel.loadSlotsForDate(teacherId, selectedDate.format(dateFormatter))
    }

    if (bookingSuccess) {
        LaunchedEffect(Unit) {
            bookingViewModel.resetBookingSuccess()
            onBookingSuccess()
        }
    }

    val isDoubleMode = isPostTrial && selectedDuration == 2
    val slotPairs = remember(availableSlots, isDoubleMode) {
        if (isDoubleMode) buildSlotPairs(availableSlots) else emptyList()
    }
    val selectedPair = if (isDoubleMode && selectedSlot != null) {
        slotPairs.firstOrNull { it.first.hour == selectedSlot!!.hour }
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Book with $teacherName", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Duration toggle — only for repeat (post-trial) bookings
            if (isPostTrial) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedDuration == 1) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable {
                                selectedDuration = 1
                                availabilityViewModel.clearSelectedSlot()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "25 min",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedDuration == 1) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedDuration == 2) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable {
                                selectedDuration = 2
                                availabilityViewModel.clearSelectedSlot()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "50 min",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedDuration == 2) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Week navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (weekOffset > 0) weekOffset-- }, enabled = weekOffset > 0) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "Previous week",
                        tint = if (weekOffset > 0) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text = "${weekStart.format(DateTimeFormatter.ofPattern("d MMM"))} — " +
                            "${weekStart.plusDays(6).format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { if (weekOffset < 1) weekOffset++ }, enabled = weekOffset < 1) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Next week",
                        tint = if (weekOffset < 1) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Day selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                weekDays.forEach { date ->
                    val isSelected = date == selectedDate
                    val isPast = date.isBefore(today)
                    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isPast -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .clickable(enabled = !isPast) { selectedDate = date }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayName.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isPast -> MaterialTheme.colorScheme.outline
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = "${date.dayOfMonth}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isPast -> MaterialTheme.colorScheme.outline
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            val selectedDayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val selectedDateFormatted = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
            Text(
                text = "$selectedDayName, $selectedDateFormatted",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Slot grid
            if (isLoading) {
                Text(
                    text = "Loading slots...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            } else if (availableSlots.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No availability on this day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (isDoubleMode) {
                if (slotPairs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No consecutive slots available for 50 min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        slotPairs.chunked(2).forEach { rowPairs ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowPairs.forEach { pair ->
                                    val isSelected = selectedPair == pair
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                width = 2.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                                        else Color.Black,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                availabilityViewModel.selectSlot(pair.first)
                                            }
                                            .padding(4.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            PairSlotBox(
                                                time = formatSlotTime(pair.first.hour),
                                                isSelected = isSelected,
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            )
                                            PairSlotBox(
                                                time = formatSlotTime(pair.second.hour),
                                                isSelected = isSelected,
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            )
                                        }
                                    }
                                }
                                if (rowPairs.size == 1) {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableSlots.chunked(4).forEach { rowSlots ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowSlots.forEach { slot ->
                                SlotChip(
                                    slot = slot,
                                    isSelected = selectedSlot == slot,
                                    onClick = {
                                        if (!slot.isBooked) {
                                            availabilityViewModel.selectSlot(slot)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Selection summary
            if (selectedSlot != null) {
                val slot = selectedSlot!!

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isDoubleMode && selectedPair != null) {
                        Text(
                            text = "Selected:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Box(
                            modifier = Modifier
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.onPrimaryContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = formatSlotTime(selectedPair.first.hour),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = formatSlotTime(selectedPair.second.hour),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Selected: ${formatSlotRange(slot.hour)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = selectedDateFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Button(
                    onClick = {
                        if (!isBookingLoading) {
                            bookingViewModel.createBooking(
                                teacherId = teacherId,
                                studentName = studentName,
                                message = "Booking request",
                                slotDate = slot.date,
                                slotHour = slot.hour,
                                durationSlots = selectedDuration
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBookingLoading
                ) {
                    if (isBookingLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Confirm Booking",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SlotLegendItem(color = Color(0xFF4CAF50), label = "Available")
                SlotLegendItem(color = Color(0xFFE53935), label = "Your booking")
                SlotLegendItem(color = MaterialTheme.colorScheme.primary, label = "Selected")
            }
        }
    }
}

@Composable
private fun PairSlotBox(time: String, isSelected: Boolean, modifier: Modifier = Modifier) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF4CAF50),
        animationSpec = tween(200),
        label = "pairSlotColor"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.White
        )
    }
}

@Composable
fun SlotChip(
    slot: AvailableSlotDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            slot.isBooked -> Color(0xFFE53935)
            else -> Color(0xFF4CAF50)
        },
        animationSpec = tween(200),
        label = "slotChipColor"
    )

    Box(
        modifier = Modifier
            .size(width = 80.dp, height = 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !slot.isBooked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatSlotTime(slot.hour),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.White
        )
    }
}

@Composable
fun SlotLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
