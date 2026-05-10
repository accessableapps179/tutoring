// Android Studio
// app/src/main/java/com/marketplace/ui/screens/NotHappyFunnelScreen.kt
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.dto.AppLanguage
import com.marketplace.dto.TeacherDto
import com.marketplace.viewmodel.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotHappyFunnelScreen(
    initialTargetLanguage: String,
    initialInstructionLanguage: String?,
    initialResults: List<TeacherDto>,
    onTeacherClick: (TeacherDto) -> Unit,
    onHomeClick: () -> Unit,
    viewModel: TeacherViewModel = viewModel()
) {
    // Start with the pre-loaded results from Session — no flash
    val freshResults by viewModel.teachers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Use fresh results if a new search was run, otherwise show initial results
    val displayTeachers = if (freshResults.isNotEmpty()) freshResults else initialResults

    var selectedTargetLanguage by remember {
        mutableStateOf(AppLanguage.all.firstOrNull { it.code == initialTargetLanguage })
    }
    var selectedInstructionLanguage by remember {
        mutableStateOf(AppLanguage.all.firstOrNull { it.code == initialInstructionLanguage })
    }
    var targetDropdownExpanded by remember { mutableStateOf(false) }
    var instructionDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Find Another Teacher", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onHomeClick) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Soft message card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🔍", style = MaterialTheme.typography.displaySmall)
                        Text(
                            text = "That tutor wasn't the right fit — it happens.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Here are some others who might be a better match for you.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Search controls
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Refine your search",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Box {
                        OutlinedButton(
                            onClick = { targetDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedTargetLanguage?.displayName ?: "I want to learn...",
                                fontWeight = FontWeight.Normal
                            )
                        }
                        DropdownMenu(
                            expanded = targetDropdownExpanded,
                            onDismissRequest = { targetDropdownExpanded = false }
                        ) {
                            AppLanguage.all.forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language.displayName) },
                                    onClick = {
                                        selectedTargetLanguage = language
                                        targetDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        OutlinedButton(
                            onClick = { instructionDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedInstructionLanguage?.displayName
                                    ?: "Taught in (any language)",
                                fontWeight = FontWeight.Normal
                            )
                        }
                        DropdownMenu(
                            expanded = instructionDropdownExpanded,
                            onDismissRequest = { instructionDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Any language") },
                                onClick = {
                                    selectedInstructionLanguage = null
                                    instructionDropdownExpanded = false
                                }
                            )
                            AppLanguage.all.forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language.displayName) },
                                    onClick = {
                                        selectedInstructionLanguage = language
                                        instructionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                selectedTargetLanguage?.let { target ->
                                    viewModel.searchTeachers(
                                        target.code,
                                        selectedInstructionLanguage?.code
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = selectedTargetLanguage != null
                        ) {
                            Text("Search", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onHomeClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Home")
                        }
                    }
                }
            }

            // Results — shown immediately from initialResults, no loading flash
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (displayTeachers.isEmpty()) {
                item {
                    Text(
                        text = "No other teachers found. Try adjusting the filters above.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(displayTeachers) { teacher ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp),
                        onClick = { onTeacherClick(teacher) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = teacher.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "£${teacher.hourlyRate} / hour",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (teacher.aboutMe.isNotBlank()) {
                                Text(
                                    text = teacher.aboutMe,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}