// app/src/main/java/com/marketplace/ui/screens/BookingScreen.kt
package com.marketplace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.dto.BookingDto
import com.marketplace.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBookingScreen(
    teacherId: String,
    teacherName: String,
    studentName: String = "",
    onBackClick: () -> Unit,
    onBookingSuccess: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val bookingSuccess by viewModel.bookingSuccess.collectAsState()

    var backClicked by remember { mutableStateOf(false) }
    val studentNameState = studentName
    var message by remember { mutableStateOf("") }

    if (bookingSuccess) {
        LaunchedEffect(Unit) {
            viewModel.resetBookingSuccess()
            onBookingSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Book ${teacherName}",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Request a lesson with $teacherName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message to teacher") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Button(
                    onClick = {
                        viewModel.createBooking(
                            teacherId = teacherId,
                            studentName = studentNameState,
                            message = message
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = message.isNotBlank()
                ) {
                    Text("Send Booking Request")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    role: String,
    onBackClick: () -> Unit,
    onStartVideoCall: (contactId: String, otherPersonName: String) -> Unit = { _, _ -> },
    viewModel: BookingViewModel = viewModel()
) {
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var backClicked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (role == "TEACHER") {
            viewModel.loadTeacherBookings()
        } else {
            viewModel.loadMyBookings()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (role == "TEACHER") "Incoming Bookings"
                        else "My Bookings",
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
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            if (!isLoading && errorMessage == null && bookings.isEmpty()) {
                Text(
                    text = "No bookings yet.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (bookings.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    items(bookings) { booking ->
                        BookingCard(
                            booking = booking,
                            role = role,
                            onConfirm = { viewModel.confirmBooking(booking.id, role) },
                            onReject = { viewModel.rejectBooking(booking.id, role) },
                            onStartVideoCall = onStartVideoCall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: BookingDto,
    role: String,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onStartVideoCall: (contactId: String, otherPersonName: String) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (booking.status) {
                "CONFIRMED" -> Color(0xFF4CAF50)
                "CANCELLED" -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (role == "TEACHER") "From: ${booking.studentName}"
                else "Booking Request",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = booking.message,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Status: ${booking.status}",
                style = MaterialTheme.typography.labelMedium,
                color = when (booking.status) {
                    "CONFIRMED" -> Color.White
                    "CANCELLED" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.secondary
                },
                fontWeight = FontWeight.Bold
            )

            if (role == "TEACHER" && booking.status == "PENDING") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = "Confirm", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(text = "Reject", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Fix #2: was passing booking.teacherId as the room ID on the teacher side.
            // The student passes booking.id as the room ID, so the teacher must match.
            if (role == "TEACHER" && booking.status == "CONFIRMED") {
                Button(
                    onClick = {
                        onStartVideoCall(
                            booking.id,          // booking.id = room ID — matches student side
                            booking.studentName
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1565C0)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.VideoCall,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "Start Video Call",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}