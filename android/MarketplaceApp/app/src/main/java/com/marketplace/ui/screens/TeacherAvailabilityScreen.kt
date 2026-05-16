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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
    var weekOffset by remember { mutableStateOf(0) }
    var pendingSlot by remember { mutableStateOf<TeacherSlotStatusDto?>(null) }
    var confirmedSlot by remember { mutableStateOf<TeacherSlotStatusDto?>(null) }
    var callInProgress by remember { mutableStateOf(false) }
    var backClicked by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()

    fun getWeekStart(offset: Int): LocalDate {
        val monday = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        return monday.plusWeeks(offset.toLong())
    }

    val weekStart = getWeekStart(weekOffset)
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (weekOffset > 0) weekOffset-- },
                    enabled = weekOffset > 0
                ) {
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

                IconButton(
                    onClick = { if (weekOffset < 12) weekOffset++ },
                    enabled = weekOffset < 12
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Next week",
                        tint = if (weekOffset < 12) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weekDays.forEach { date ->
                    val isSelected = date == selectedDate
                    val isPast = date.isBefore(today)
                    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    val chipPagWeek = minOf(4, (date.dayOfMonth - 1) / 7 + 1)
                    val isFirstOfPagWeek = date.dayOfMonth == 1 ||
                        minOf(4, (date.dayOfMonth - 2) / 7 + 1) != chipPagWeek

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(0.dp))
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isPast     -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else       -> MaterialTheme.colorScheme.surfaceVariant
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
                                isPast     -> MaterialTheme.colorScheme.outline
                                else       -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = "${date.dayOfMonth}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isPast     -> MaterialTheme.colorScheme.outline
                                else       -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = if (isFirstOfPagWeek) "Wk$chipPagWeek" else "",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isFirstOfPagWeek) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isSelected          -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                isFirstOfPagWeek    -> Color(0xFF1565C0)
                                else                -> MaterialTheme.colorScheme.surface
                            }
                        )
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
            .size(width = 80.dp, height = 44.dp)
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
            .size(width = 80.dp, height = 44.dp)
            .clip(RoundedCornerShape(0.dp))
            .background(bgColor)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(0.dp))
            .clickable(enabled = isClickable) { onClick() }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = when {
                slot.status == "PENDING"   -> "${formatSlotTime(slot.hour)} ?"
                slot.status == "CONFIRMED" -> "${formatSlotTime(slot.hour)} ✓"
                else                       -> formatSlotTime(slot.hour)
            },
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
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