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
import androidx.compose.material.icons.filled.CalendarMonth
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
    onCalendarClick: (() -> Unit)? = null,
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

    var selectedDate by remember { mutableStateOf(Session.pendingBookingDate ?: LocalDate.now()) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    LaunchedEffect(Unit) {
        Session.pendingBookingDate = null
        availabilityViewModel.clearSelectedSlot()
        availabilityViewModel.loadHourRange(teacherId)
        availabilityViewModel.loadSlotsForDate(teacherId, selectedDate.format(dateFormatter))
    }

    LaunchedEffect(selectedDate) {
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

    val bookedHours = availableSlots.filter { it.isBooked }.map { it.hour }.toSet()
    val splitDoubleSecondHours = availableSlots
        .filter { it.isBooked && it.bookedDuration >= 2 }
        .map { it.hour + 0.5 }
        .toSet()

    // Chunked into rows of 4, but a booked double-first is never allowed to be
    // the last slot in a row — bump it to start the next row so the pair always
    // lands in the same row and renders as the normal merged block.
    val smartChunks: List<List<AvailableSlotDto>> = buildList {
        val current = mutableListOf<AvailableSlotDto>()
        for (slot in availableSlots) {
            if (current.size == 3 && slot.isBooked && slot.bookedDuration >= 2) {
                add(current.toList())
                current.clear()
            }
            current.add(slot)
            if (current.size == 4) {
                add(current.toList())
                current.clear()
            }
        }
        if (current.isNotEmpty()) add(current.toList())
    }

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
                actions = {
                    if (onCalendarClick != null) {
                        IconButton(onClick = onCalendarClick) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Month view",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
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


            val selectedDayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val selectedDateFormatted = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
            Text(
                text = "$selectedDayName, $selectedDateFormatted",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // Selection card — always visible, chips never shift
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedSlot != null) {
                    val slot = selectedSlot!!
                    val durationMinutes = if (isDoubleMode) 50 else 25
                    Text(
                        text = formatLessonRange(slot.hour, durationMinutes),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Button(
                        onClick = {
                            if (!isBookingLoading) {
                                bookingViewModel.createBooking(
                                    teacherId    = teacherId,
                                    studentName  = studentName,
                                    message      = "Booking request",
                                    slotDate     = slot.date,
                                    slotHour     = slot.hour,
                                    durationSlots = selectedDuration
                                )
                            }
                        },
                        enabled = !isBookingLoading
                    ) {
                        if (isBookingLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                color       = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Confirm", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Pick a time",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                }
            }

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
                    smartChunks.forEach { rowSlots ->
                        val firstIdx = if (selectedFirstHour != null)
                            rowSlots.indexOfFirst { it.hour == selectedFirstHour } else -1
                        val secondIdx = if (selectedSecondHour != null)
                            rowSlots.indexOfFirst { it.hour == selectedSecondHour } else -1
                        val pairInRow = firstIdx >= 0 && secondIdx == firstIdx + 1

                        val bookedFirstIdx = rowSlots.indexOfFirst { it.isBooked && it.bookedDuration >= 2 }
                        val bookedPairInRow = bookedFirstIdx >= 0 && bookedFirstIdx + 1 < rowSlots.size

                        if (pairInRow) {
                            // Row contains consecutive selected pair — draw border around them
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // chips before the pair — render booked pairs as merged block
                                var bIdx = 0
                                while (bIdx < firstIdx) {
                                    val s = rowSlots[bIdx]
                                    if (s.isBooked && s.bookedDuration >= 2 &&
                                            bIdx + 1 < firstIdx && rowSlots[bIdx + 1].isBooked) {
                                        Box(
                                            modifier = Modifier
                                                .weight(2f).height(88.dp)
                                                .background(Color(0xFFE53935))
                                                .drawWithContent {
                                                    drawContent()
                                                    drawLine(Color.White.copy(alpha = 0.25f),
                                                        Offset(size.width / 2f, 0f),
                                                        Offset(size.width / 2f, size.height),
                                                        strokeWidth = 1.dp.toPx(), cap = StrokeCap.Butt)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("${formatSlotTime(s.hour)}▶${formatLessonEnd(s.hour, 50)}",
                                                fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                                        }
                                        bIdx += 2
                                    } else {
                                        DoubleChip(slot = s, isSelected = false,
                                            splitStartHour = if (s.isBooked && s.hour in splitDoubleSecondHours) s.hour - 0.5 else null,
                                            freeHours = freeHours, onSelect = { availabilityViewModel.selectSlot(it) })
                                        bIdx += 1
                                    }
                                }
                                // bordered pair
                                val secondSlot = rowSlots[secondIdx]
                                val secondCanShift = !secondSlot.isBooked &&
                                        (secondSlot.hour + 0.5) in freeHours
                                val secondIsLightBlue = (secondSlot.hour + 0.5) !in freeHours
                                Box(
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(88.dp)
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
                                        fontSize = 22.sp,
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
                                            fontSize = 18.sp,
                                            lineHeight = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD600),
                                            modifier = Modifier.align(Alignment.BottomEnd).padding(0.dp)
                                        )
                                    }
                                }
                                // chips after the pair — render booked pairs as merged block
                                var aIdx = secondIdx + 1
                                while (aIdx < rowSlots.size) {
                                    val s = rowSlots[aIdx]
                                    if (s.isBooked && s.bookedDuration >= 2 &&
                                            aIdx + 1 < rowSlots.size && rowSlots[aIdx + 1].isBooked) {
                                        Box(
                                            modifier = Modifier
                                                .weight(2f).height(88.dp)
                                                .background(Color(0xFFE53935))
                                                .drawWithContent {
                                                    drawContent()
                                                    drawLine(Color.White.copy(alpha = 0.25f),
                                                        Offset(size.width / 2f, 0f),
                                                        Offset(size.width / 2f, size.height),
                                                        strokeWidth = 1.dp.toPx(), cap = StrokeCap.Butt)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("${formatSlotTime(s.hour)}▶${formatLessonEnd(s.hour, 50)}",
                                                fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                                        }
                                        aIdx += 2
                                    } else {
                                        DoubleChip(slot = s, isSelected = false,
                                            splitStartHour = if (s.isBooked && s.hour in splitDoubleSecondHours) s.hour - 0.5 else null,
                                            freeHours = freeHours, onSelect = { availabilityViewModel.selectSlot(it) })
                                        aIdx += 1
                                    }
                                }
                                // invisible spacers so all rows have the same total weight (4)
                                val filled = (firstIdx) + 2 + (rowSlots.size - secondIdx - 1)
                                repeat(4 - filled) { Box(modifier = Modifier.weight(1f)) }
                            }
                        } else if (bookedPairInRow) {
                            // Booked pair — merged red block with arrow and time range
                            val bSecondIdx = bookedFirstIdx + 1
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (i in 0 until bookedFirstIdx) {
                                    val s = rowSlots[i]
                                    val isFirstChip = s.hour == selectedFirstHour
                                    val isSecondChip = s.hour == selectedSecondHour
                                    DoubleChip(
                                        slot = s,
                                        isSelected = isFirstChip || isSecondChip,
                                        isFirstOfPair = isFirstChip,
                                        isFirstOfBookedPair = s.isBooked && s.bookedDuration >= 2,
                                        pairedStartHour = if (isSecondChip) selectedFirstHour else null,
                                        splitStartHour = if (s.isBooked && s.hour in splitDoubleSecondHours) s.hour - 0.5 else null,
                                        freeHours = freeHours,
                                        onSelect = { availabilityViewModel.selectSlot(it) },
                                        onDeselect = { availabilityViewModel.clearSelectedSlot() }
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(88.dp)
                                        .background(Color(0xFFE53935))
                                        .drawWithContent {
                                            drawContent()
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
                                        text = "${formatSlotTime(rowSlots[bookedFirstIdx].hour)}▶${formatLessonEnd(rowSlots[bookedFirstIdx].hour, 50)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = Color.White
                                    )
                                }
                                for (i in bSecondIdx + 1 until rowSlots.size) {
                                    val s = rowSlots[i]
                                    val isFirstChip = s.hour == selectedFirstHour
                                    val isSecondChip = s.hour == selectedSecondHour
                                    DoubleChip(
                                        slot = s,
                                        isSelected = isFirstChip || isSecondChip,
                                        isFirstOfPair = isFirstChip,
                                        isFirstOfBookedPair = s.isBooked && s.bookedDuration >= 2,
                                        pairedStartHour = if (isSecondChip) selectedFirstHour else null,
                                        splitStartHour = if (s.isBooked && s.hour in splitDoubleSecondHours) s.hour - 0.5 else null,
                                        freeHours = freeHours,
                                        onSelect = { availabilityViewModel.selectSlot(it) },
                                        onDeselect = { availabilityViewModel.clearSelectedSlot() }
                                    )
                                }
                                val bFilled = bookedFirstIdx + 2 + (rowSlots.size - bSecondIdx - 1)
                                repeat(4 - bFilled) { Box(modifier = Modifier.weight(1f)) }
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
                                        isFirstOfBookedPair = slot.isBooked && slot.bookedDuration >= 2,
                                        pairedStartHour = if (isSecondChip) selectedFirstHour else null,
                                        splitStartHour = if (slot.isBooked && slot.hour in splitDoubleSecondHours) slot.hour - 0.5 else null,
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
                // Single mode: weight-based chips to match double-mode alignment
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    smartChunks.forEach { rowSlots ->
                        val sBookedFirstIdx = rowSlots.indexOfFirst { it.isBooked && it.bookedDuration >= 2 }
                        val sBookedPairInRow = sBookedFirstIdx >= 0 && sBookedFirstIdx + 1 < rowSlots.size
                        val sIsSplitFirst = sBookedFirstIdx >= 0 && sBookedFirstIdx + 1 >= rowSlots.size

                        if (sBookedPairInRow) {
                            val sSecondIdx = sBookedFirstIdx + 1
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (i in 0 until sBookedFirstIdx) {
                                    SlotChip(
                                        slot = rowSlots[i],
                                        isSelected = selectedSlot == rowSlots[i],
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            if (!rowSlots[i].isBooked) {
                                                if (selectedSlot == rowSlots[i]) availabilityViewModel.clearSelectedSlot()
                                                else availabilityViewModel.selectSlot(rowSlots[i])
                                            }
                                        }
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(88.dp)
                                        .background(Color(0xFFE53935))
                                        .drawWithContent {
                                            drawContent()
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
                                        text = "${formatSlotTime(rowSlots[sBookedFirstIdx].hour)}▶${formatLessonEnd(rowSlots[sBookedFirstIdx].hour, 50)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = Color.White
                                    )
                                }
                                for (i in sSecondIdx + 1 until rowSlots.size) {
                                    SlotChip(
                                        slot = rowSlots[i],
                                        isSelected = selectedSlot == rowSlots[i],
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            if (!rowSlots[i].isBooked) {
                                                if (selectedSlot == rowSlots[i]) availabilityViewModel.clearSelectedSlot()
                                                else availabilityViewModel.selectSlot(rowSlots[i])
                                            }
                                        }
                                    )
                                }
                                val sFilled = sBookedFirstIdx + 2 + (rowSlots.size - sSecondIdx - 1)
                                repeat(4 - sFilled) { Box(modifier = Modifier.weight(1f).height(88.dp)) }
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowSlots.forEachIndexed { idx, slot ->
                                    val isSplitFirstChip = sIsSplitFirst && idx == sBookedFirstIdx
                                    val isSplitSecondChip = slot.isBooked && slot.hour in splitDoubleSecondHours
                                    when {
                                        isSplitFirstChip -> {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(88.dp)
                                                    .background(Color(0xFFE53935)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = formatSlotTime(slot.hour),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 24.sp,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "▶",
                                                    fontSize = 16.sp,
                                                    color = Color.White,
                                                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                                                )
                                            }
                                        }
                                        isSplitSecondChip -> {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(88.dp)
                                                    .background(Color(0xFFE53935)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = formatLessonEnd(slot.hour - 0.5, 50),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 24.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        else -> {
                                            SlotChip(
                                                slot = slot,
                                                isSelected = selectedSlot == slot,
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    if (!slot.isBooked) {
                                                        if (selectedSlot == slot) availabilityViewModel.clearSelectedSlot()
                                                        else availabilityViewModel.selectSlot(slot)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                                repeat(4 - rowSlots.size) {
                                    Box(modifier = Modifier.weight(1f).height(88.dp))
                                }
                            }
                        }
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
        Text(text = time, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
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
    isFirstOfBookedPair: Boolean = false,
    pairedStartHour: Double? = null,
    splitStartHour: Double? = null,
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
            .height(88.dp)
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
            splitStartHour != null -> formatLessonEnd(splitStartHour, 50)
            else -> formatSlotTime(slot.hour)
        }
        Text(text = chipText, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
        if ((isSelected && isFirstOfPair) || isFirstOfBookedPair) {
            Text(
                text = "▶",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.align(Alignment.CenterEnd)
                    .padding(end = if (slot.hour < 10.0) 6.dp else 0.dp)
            )
        }
        if (isSelected && pairedStartHour != null && canStartDouble) {
            Text(
                text = "${formatSlotTime(slot.hour)}▶",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD600),
                modifier = Modifier.align(Alignment.BottomEnd).padding(0.dp)
            )
        }
        if (!canStartDouble && !isFirstOfPair && !slot.isBooked && pairedStartHour == null) {
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .height(88.dp)
            .then(if (isSelected) Modifier.drawWithContent {
                drawContent()
                val sw = 4.dp.toPx()
                val half = sw / 2f
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(-half, -half),
                    size = Size(size.width + sw, size.height + sw),
                    style = Stroke(width = sw)
                )
            } else Modifier)
            .clip(RoundedCornerShape(0.dp))
            .background(bgColor)
            .clickable(enabled = !slot.isBooked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatSlotTime(slot.hour),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
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
