package com.marketplace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.dto.AppLanguage
import com.marketplace.viewmodel.TrialResultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDetailScreen(
    teacherId: String,
    teacherName: String,
    teacherHourlyRate: Double,
    teacherAboutMe: String,
    teachingLanguages: List<String> = emptyList(),
    instructionLanguages: List<String> = emptyList(),
    role: String = "",
    userId: String = "",
    onBackClick: () -> Unit,
    onBookClick: (() -> Unit)? = null,
    onMessageClick: ((contactId: String) -> Unit)? = null,
    trialResultViewModel: TrialResultViewModel = viewModel()
) {
    val canBook by trialResultViewModel.canBook.collectAsState()
    val isLoading by trialResultViewModel.isLoading.collectAsState()
    val hasTrialResult by trialResultViewModel.hasTrialResult.collectAsState()
    val contactUnlocked by trialResultViewModel.contactUnlocked.collectAsState()

    var backClicked by remember { mutableStateOf(false) }

    LaunchedEffect(teacherId) {
        if (role == "STUDENT") {
            trialResultViewModel.checkCanBook(teacherId)
            trialResultViewModel.checkTrialStatus(teacherId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(teacherName, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name + rate banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = teacherName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "£${teacherHourlyRate} / hour",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            HorizontalDivider()

            // About Me
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "About Me",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = teacherAboutMe,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 24.sp
                )
            }

            // Languages
            if (teachingLanguages.isNotEmpty() || instructionLanguages.isNotEmpty()) {
                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Languages",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (teachingLanguages.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Teaches",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            teachingLanguages.chunked(3).forEach { rowCodes ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rowCodes.forEach { code ->
                                        val displayName = AppLanguage.fromCode(code)?.displayName ?: code
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            tonalElevation = 2.dp
                                        ) {
                                            Text(
                                                text = displayName,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (instructionLanguages.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Lesson language",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            instructionLanguages.chunked(3).forEach { rowCodes ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rowCodes.forEach { code ->
                                        val displayName = AppLanguage.fromCode(code)?.displayName ?: code
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            tonalElevation = 2.dp
                                        ) {
                                            Text(
                                                text = displayName,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Student actions
            if (role == "STUDENT") {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                        // If contact is unlocked (was happy after trial) show message button
                        if (contactUnlocked) {
                            Button(
                                onClick = {
                                    onMessageClick?.invoke(teacherId)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text(
                                    text = "💬 Message",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Book button — only shown if no trial result with this teacher yet
                        when {
                            canBook == false -> {
                                Text(
                                    text = "You have already had a trial lesson with $teacherName.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            onBookClick != null -> {
                                Button(
                                    onClick = onBookClick,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "📅 Book a Trial Lesson",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}