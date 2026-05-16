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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    var selectedDuration by remember { mutableStateOf(2) }

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

    // For double mode: hours of the selected pair
    val selectedFirstHour = if (isDoubleMode) selectedSlot?.hour else null
    val selectedSecondHour = selectedFirstHour?.plus(0.5)

    // Set of free hours in double mode (used to decide if a chip can start a double)
    val freeHours = if (isDoubleMode) {
        availableSlots.filter { !it.isBooked }.map { it.hour }.toSet()
    } else emptySet()

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
                // Double mode: normal grid, rectangle appears only around the selected pair
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableSlots.chunked(4).forEach { rowSlots ->
                        val firstIdx = if (selectedFirstHour != null)
                            rowSlots.indexOfFirst { it.hour == selectedFirstHour } else -1
                        val secondIdx = if (selectedSecondHour != null)
                            rowSlots.indexOfFirst { it.hour == selectedSecondHour } else -1
                        val pairInRow = firstIdx >= 0 && secondIdx == firstIdx + 1

                        if (pairInRow) {
                            // Row contains consecutive selected pair — draw border around them
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // chips before the pair
                                for (i in 0 until firstIdx) {
                                    DoubleChip(
                                        slot = rowSlots[i],
                                        isSelected = false,
                                        freeHours = freeHours,
                                        onSelect = { availabilityViewModel.selectSlot(it) }
                                    )
                                }
                                // bordered pair
                                val secondSlot = rowSlots[secondIdx]
                                val secondCanShift = !secondSlot.isBooked &&
                                        (secondSlot.hour + 0.5) in freeHours
                                val secondIsLightBlue = (secondSlot.hour + 0.5) !in freeHours
                                Box(
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(80.dp)
                                        .clickable { availabilityViewModel.clearSelectedSlot() }
                                        .background(MaterialTheme.colorScheme.primary)
                                        .drawWithContent {
                                            drawContent()
                                            val sw = 4.dp.toPx()
                                            val half = sw / 2f
                                            drawRect(
                                                color = Color.Black,
                                                topLeft = Offset(-half, -half),
                                                size = Size(size.width + sw, size.height + sw),
                                                style = Stroke(width = sw)
                                            )
                                            drawLine(
                                                color = Color.White.copy(alpha = 0.25f),
                                                start = Offset(size.width / 2f, 0f),
                                                end = Offset(size.width / 2f, size.height),
                                                strokeWidth = 1.dp.toPx(),
                                                cap = StrokeCap.Butt
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${formatSlotTime(rowSlots[firstIdx].hour)}▶${formatLessonEnd(rowSlots[firstIdx].hour, 50)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                    if (secondCanShift) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(0.5f)
                                                .align(Alignment.CenterEnd)
                                                .background(Color.White.copy(alpha = 0.08f))
                                                .clickable { availabilityViewModel.selectSlot(secondSlot) }
                                        )
                                        Text(
                                            text = "${formatSlotTime(secondSlot.hour)}▶",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD600),
                                            modifier = Modifier.align(Alignment.BottomEnd).padding(0.dp)
                                        )
                                    }
                                }
                                // chips after the pair
                                for (i in secondIdx + 1 until rowSlots.size) {
                                    DoubleChip(
                                        slot = rowSlots[i],
                                        isSelected = false,
                                        freeHours = freeHours,
                                        onSelect = { availabilityViewModel.selectSlot(it) }
                                    )
                                }
                                // invisible spacers so all rows have the same total weight (4)
                                val filled = (firstIdx) + 2 + (rowSlots.size - secondIdx - 1)
                                repeat(4 - filled) { Box(modifier = Modifier.weight(1f)) }
                            }
                        } else {
                            // Normal row — no pair border
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowSlots.forEach { slot ->
                                    val isFirstChip = slot.hour == selectedFirstHour
                                    val isSecondChip = slot.hour == selectedSecondHour
                                    DoubleChip(
                                        slot = slot,
                                        isSelected = isFirstChip || isSecondChip,
                                        isFirstOfPair = isFirstChip,
                                        pairedStartHour = if (isSecondChip) selectedFirstHour else null,
                                        freeHours = freeHours,
                                        onSelect = { availabilityViewModel.selectSlot(it) },
                                        onDeselect = { availabilityViewModel.clearSelectedSlot() }
                                    )
                                }
                                // pad last row to keep chip widths consistent
                                repeat(4 - rowSlots.size) { Box(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            } else {
                // Single mode: 4-per-row fixed-size chips
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableSlots.chunked(4).forEach { rowSlots ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowSlots.forEach { slot ->
                                SlotChip(
                                    slot = slot,
                                    isSelected = selectedSlot == slot,
                                    onClick = {
                                        if (!slot.isBooked) availabilityViewModel.selectSlot(slot)
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
                    val durationMinutes = if (isDoubleMode) 50 else 25
                    Text(
                        text = "Selected: ${formatLessonRange(slot.hour, durationMinutes)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (isDoubleMode) {
                        Text(
                            text = formatSlotTime(slot.hour + 0.5),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = selectedDateFormatted,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
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

// Chip used inside the bordered pair box
@Composable
private fun DoubleChipInner(
    time: String,
    isSelected: Boolean,
    isSecondChip: Boolean = false,
    subTime: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            else -> Color(0xFF4CAF50)
        },
        animationSpec = tween(200),
        label = "doubleChipInner"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(0.dp))
            .background(bgColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(text = time, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
        if (subTime != null) {
            Text(
                text = subTime,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp)
            )
        }
        if (isSecondChip) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(Color(0xFFE53935))
            )
        }
    }
}

// Chip used in double mode for non-paired slots — weight-based sizing
@Composable
private fun RowScope.DoubleChip(
    slot: AvailableSlotDto,
    isSelected: Boolean,
    isFirstOfPair: Boolean = false,
    pairedStartHour: Double? = null,
    freeHours: Set<Double>,
    onSelect: (AvailableSlotDto) -> Unit,
    onDeselect: (() -> Unit)? = null
) {
    val canStartDouble = !slot.isBooked && (slot.hour + 0.5) in freeHours
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            slot.isBooked -> Color(0xFFE53935)
            else -> Color(0xFF4CAF50)
        },
        animationSpec = tween(200),
        label = "doubleChipColor"
    )
    Box(
        modifier = Modifier
            .weight(1f)
            .height(80.dp)
            .then(if (isSelected) Modifier.drawWithContent {
                drawContent()
                val sw = 4.dp.toPx()
                val w = size.width
                val h = size.height
                val path = Path()
                if (isFirstOfPair) {
                    // open right: top + left + bottom, no right vertical
                    path.moveTo(w, 0f)
                    path.lineTo(0f, 0f)
                    path.lineTo(0f, h)
                    path.lineTo(w, h)
                } else {
                    // open left: top + right + bottom, no left vertical
                    path.moveTo(0f, 0f)
                    path.lineTo(w, 0f)
                    path.lineTo(w, h)
                    path.lineTo(0f, h)
                }
                drawPath(path, Color.Black, style = Stroke(width = sw))
            } else Modifier)
            .clip(RoundedCornerShape(0.dp))
            .background(bgColor)
            .clickable(enabled = canStartDouble || (isSelected && isFirstOfPair)) {
                if (isSelected && isFirstOfPair) onDeselect?.invoke() else onSelect(slot)
            },
        contentAlignment = Alignment.Center
    ) {
        val chipText = when {
            isSelected && pairedStartHour != null -> formatLessonEnd(pairedStartHour, 50)
            else -> formatSlotTime(slot.hour)
        }
        Text(text = chipText, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
        if (isSelected && isFirstOfPair) {
            Text(
                text = "▶",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
            )
        }
        if (isSelected && pairedStartHour != null && canStartDouble) {
            Text(
                text = "${formatSlotTime(slot.hour)}▶",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD600),
                modifier = Modifier.align(Alignment.BottomEnd).padding(0.dp)
            )
        }
        if (!canStartDouble && !isFirstOfPair && !slot.isBooked) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(Color(0xFFE53935))
            )
        }
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
            .clip(RoundedCornerShape(0.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(0.dp)
            )
            .clickable(enabled = !slot.isBooked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatSlotTime(slot.hour),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
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
