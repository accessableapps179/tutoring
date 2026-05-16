// app/src/main/java/com/marketplace/ui/screens/TeacherAvailabilityScreen.kt
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.marketplace.api.RetrofitClient
import com.marketplace.dto.TeacherSlotStatusDto
import com.marketplace.viewmodel.AvailabilityViewModel
import com.marketplace.viewmodel.BookingViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

val SlotGrey   = Color(0xFF9E9E9E)
val SlotBlue   = Color(0xFF1565C0)
val SlotYellow = Color(0xFFF9A825)
val SlotGreen  = Color(0xFF2E7D32)
val SlotPurple = Color(0xFF7B1FA2)
val SlotBrown  = Color(0xFF6D4C41)

fun formatSlotTime(hour: Double): String {
    val h = hour.toInt()
    val m = if (hour % 1.0 == 0.5) "30" else "00"
    return "${h}:${m}"
}

fun formatSlotRange(hour: Double): String {
    val end = hour + 0.5
    return "${formatSlotTime(hour)} — ${formatSlotTime(end)}"
}

fun formatLessonEnd(startHour: Double, durationMinutes: Int): String {
    val totalMinutes = (startHour * 60).toInt() + durationMinutes
    val endH = totalMinutes / 60
    val endM = totalMinutes % 60
    return "$endH:${endM.toString().padStart(2, '0')}"
}

fun formatLessonRange(startHour: Double, durationMinutes: Int): String {
    return "${formatSlotTime(startHour)} — ${formatLessonEnd(startHour, durationMinutes)}"
}

/** Checks /call-status/{roomId} and returns true if a call is currently in progress. */
suspend fun isCallInProgress(bookingId: String): Boolean {
    return try {
        val roomId = bookingId.take(12)
        val response = RetrofitClient.callStatusApi.getCallStatus(roomId)
        response.exists && response.peerCount > 0
    } catch (e: Exception) {
        false
    }
}

val SlotOrange = Color(0xFFE65100)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAvailabilityScreen(
    onBackClick: () -> Unit,
    onEditTemplateClick: () -> Unit = {},
    onStartVideoCall: (contactId: String, otherPersonName: String) -> Unit = { _, _ -> },
    onRejoinCall: (contactId: String, otherPersonName: String) -> Unit = { _, _ -> },
    availabilityViewModel: AvailabilityViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel()
) {
    val teacherDaySlots by availabilityViewModel.teacherDaySlots.collectAsState()
    val isLoading by availabilityViewModel.isLoading.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }
    var pendingSlot by remember { mutableStateOf<TeacherSlotStatusDto?>(null) }
    var confirmedSlot by remember { mutableStateOf<TeacherSlotStatusDto?>(null) }
    var callInProgress by remember { mutableStateOf(false) }
    var backClicked by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()

    LaunchedEffect(selectedDate) {
        availabilityViewModel.loadTeacherDayView(selectedDate.format(dateFormatter))
    }

    // ─── Pending booking dialog ───────────────────────────────
    if (pendingSlot != null) {
        val slot = pendingSlot!!
        AlertDialog(
            onDismissRequest = { pendingSlot = null },
            title = { Text(text = "Booking Request", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "From: ${slot.studentName ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Time: ${formatSlotRange(slot.hour)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        slot.bookingId?.let { bookingId ->
                            bookingViewModel.confirmBooking(bookingId, "TEACHER")
                        }
                        pendingSlot = null
                        availabilityViewModel.refreshTeacherDayView(selectedDate.format(dateFormatter))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SlotGreen)
                ) {
                    Text("Confirm", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        slot.bookingId?.let { bookingId ->
                            bookingViewModel.rejectBooking(bookingId, "TEACHER")
                        }
                        pendingSlot = null
                        availabilityViewModel.refreshTeacherDayView(selectedDate.format(dateFormatter))
                    }
                ) {
                    Text("Decline", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    // ─── Confirmed booking dialog ─────────────────────────────
    if (confirmedSlot != null) {
        val slot = confirmedSlot!!
        val bookingId = slot.bookingId

        AlertDialog(
            onDismissRequest = { confirmedSlot = null },
            title = { Text(text = "Confirmed Lesson", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Student: ${slot.studentName ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Time: ${formatSlotRange(slot.hour)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                if (bookingId != null) {
                    // Show "Rejoin Call" if a call is already in progress,
                    // "Start Video Call" if not. callInProgress is checked when
                    // the slot is tapped, before the dialog opens.
                    Button(
                        onClick = {
                            confirmedSlot = null
                            val studentName = slot.studentName ?: "Student"
                            if (callInProgress) {
                                // Rejoin: skip the lobby, go straight into the signal room
                                onRejoinCall(bookingId, studentName)
                            } else {
                                // Fresh start: go through the lobby
                                onStartVideoCall(bookingId, studentName)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SlotBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VideoCall,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp),
                            tint = Color.White
                        )
                        Text(
                            text = if (callInProgress) "Rejoin Call" else "Start Video Call",
                            color = Color.White
                        )
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmedSlot = null }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Calendar", fontWeight = FontWeight.Bold) },
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
                actions = {},
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
            val calFirstDay    = displayMonth.atDay(1)
            val calStartOffset = calFirstDay.dayOfWeek.value - 1
            val calDaysInMonth = displayMonth.lengthOfMonth()
            val calRows        = (calStartOffset + calDaysInMonth + 6) / 7
            val monthLabel     = displayMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                    "  ${displayMonth.year}"

            // ─── Month navigation ─────────────────────────────────────
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

            // ─── Day-of-week headers ──────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
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
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Black))

            // ─── Calendar grid ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp * calRows)
            ) {
                repeat(calRows) { rowIdx ->
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        repeat(7) { colIdx ->
                            val dayNum     = rowIdx * 7 + colIdx - calStartOffset + 1
                            val validDay   = dayNum in 1..calDaysInMonth
                            val calDate    = if (validDay) displayMonth.atDay(dayNum) else null
                            val isPast     = calDate?.isBefore(today) == true
                            val isToday    = calDate == today
                            val isSelected = calDate == selectedDate

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .border(1.5.dp, Color(0xFFCCCCCC))
                                    .then(
                                        if (validDay && !isPast)
                                            Modifier.clickable { selectedDate = calDate!! }
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (validDay) {
                                    if (isSelected || isToday) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                    else Color.Black
                                                )
                                        )
                                    }
                                    Text(
                                        text = dayNum.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = when {
                                            isSelected || isToday -> Color.White
                                            isPast -> Color(0xFFBBBBBB)
                                            else   -> Color.Black
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val selectedDayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val selectedDateFormatted = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
            val pagWeek = minOf(4, (selectedDate.dayOfMonth - 1) / 7 + 1)
            Text(
                text = "$selectedDayName, $selectedDateFormatted · Wk$pagWeek",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (isLoading) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    teacherDaySlots.chunked(4).forEach { rowSlots ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowSlots.forEach { slot ->
                                val now = LocalDate.now()
                                val currentMinute = LocalTime.now().hour * 60 + LocalTime.now().minute
                                val slotMinute = (slot.hour * 60).toInt()
                                val slotIsPast = selectedDate.isBefore(now) ||
                                        (selectedDate == now && slotMinute <= currentMinute)

                                TeacherSlotChip(
                                    slot = slot,
                                    slotIsPast = slotIsPast,
                                    onClick = {
                                        when (slot.status) {
                                            "PENDING"   -> pendingSlot = slot
                                            "CONFIRMED" -> {
                                                // Check call status before opening the dialog
                                                // so we know whether to show Start or Rejoin
                                                scope.launch {
                                                    callInProgress = slot.bookingId?.let {
                                                        isCallInProgress(it)
                                                    } ?: false
                                                    confirmedSlot = slot
                                                }
                                            }
                                            else -> if (!slotIsPast) {
                                                availabilityViewModel.toggleTeacherSlot(
                                                    selectedDate.format(dateFormatter),
                                                    slot.hour
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendDot(color = SlotGrey,   label = "Off")
                LegendDot(color = SlotBlue,   label = "Available")
                LegendDot(color = SlotYellow, label = "Pending")
                LegendDot(color = SlotGreen,  label = "Confirmed")
                LegendDot(color = SlotPurple, label = "Happy")
                LegendDot(color = SlotBrown,  label = "Not happy")
            }
        }
    }
}

@Composable
fun TeacherSlotChip(
    slot: TeacherSlotStatusDto,
    slotIsPast: Boolean,
    onClick: () -> Unit
) {
    val isTrialCompleted = slot.status == "TRIAL_COMPLETED_HAPPY" ||
            slot.status == "TRIAL_COMPLETED_UNHAPPY"

    val bgColor by animateColorAsState(
        targetValue = when {
            slot.status == "TRIAL_COMPLETED_HAPPY"   -> SlotPurple
            slot.status == "TRIAL_COMPLETED_UNHAPPY" -> SlotBrown
            slotIsPast                               -> SlotGrey
            slot.status == "AVAILABLE"               -> SlotBlue
            slot.status == "PENDING"                 -> SlotYellow
            slot.status == "CONFIRMED"               -> SlotGreen
            else                                     -> SlotGrey
        },
        animationSpec = tween(200),
        label = "teacherSlotColor"
    )

    val isClickable = !isTrialCompleted && (!slotIsPast || slot.status == "CONFIRMED")

    val conflictBorder = slot.conflictsWithPag &&
            (slot.status == "PENDING" || slot.status == "CONFIRMED")

    val modifier = if (isTrialCompleted) {
        Modifier
            .size(width = 88.dp, height = 88.dp)
            .clip(RoundedCornerShape(0.dp))
            .background(bgColor)
    } else {
        val borderWidth = when {
            conflictBorder -> 3.dp
            slot.status == "PENDING" && !slotIsPast -> 2.dp
            else -> 0.dp
        }
        val borderColor = when {
            conflictBorder -> SlotOrange
            else -> Color.White.copy(alpha = 0.5f)
        }
        Modifier
            .size(width = 88.dp, height = 88.dp)
            .clip(RoundedCornerShape(0.dp))
            .background(bgColor)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(0.dp))
            .clickable(enabled = isClickable) { onClick() }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = formatSlotTime(slot.hour),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.White
        )
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}