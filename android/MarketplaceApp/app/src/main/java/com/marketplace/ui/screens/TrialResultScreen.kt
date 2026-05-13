package com.marketplace.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.Session
import com.marketplace.viewmodel.BookingViewModel
import com.marketplace.viewmodel.TrialResultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrialResultScreen(
    bookingId: String,
    teacherId: String,
    teacherName: String,
    onHappyContactUnlocked: () -> Unit,
    onNotHappy: () -> Unit,
    trialResultViewModel: TrialResultViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel()
) {
    val isLoading by trialResultViewModel.isLoading.collectAsState()
    val errorMessage by trialResultViewModel.errorMessage.collectAsState()
    val submitSuccess by trialResultViewModel.submitSuccess.collectAsState()
    val contactUnlocked by trialResultViewModel.contactUnlocked.collectAsState()
    val contactId by trialResultViewModel.contactId.collectAsState()

    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            trialResultViewModel.resetSubmitSuccess()
            // Refresh bookings on both sides so cancelled booking disappears
            bookingViewModel.loadUpcomingBookings()
            bookingViewModel.loadTeacherBookings()
            if (contactUnlocked) {
                Session.pendingContactId = contactId
                onHappyContactUnlocked()
            } else {
                onNotHappy()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trial Lesson",
                        fontWeight = FontWeight.Bold
                    )
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
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "🎓",
                        fontSize = 64.sp
                    )

                    Text(
                        text = "How was your trial lesson?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Were you happy with $teacherName?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "If yes, you will be able to message $teacherName and book future lessons.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                trialResultViewModel.submitTrialResult(
                                    bookingId = bookingId,
                                    teacherId = teacherId,
                                    happy = false
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SentimentDissatisfied,
                                    contentDescription = "Not happy",
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Not Happy",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                trialResultViewModel.submitTrialResult(
                                    bookingId = bookingId,
                                    teacherId = teacherId,
                                    happy = true
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SentimentVerySatisfied,
                                    contentDescription = "Happy",
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.White
                                )
                                Text(
                                    text = "Happy!",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Text(
                        text = "If you are not happy, this teacher will not be available for future bookings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}