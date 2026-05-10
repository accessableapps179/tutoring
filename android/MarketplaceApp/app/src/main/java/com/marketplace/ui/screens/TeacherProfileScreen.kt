// MODIFIED FILE
// app/src/main/java/com/marketplace/ui/screens/TeacherProfileScreen.kt
package com.marketplace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.dto.AppLanguage
import com.marketplace.viewmodel.TeacherProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreen(
    userId: String,
    userName: String = "",
    onBackClick: () -> Unit,
    viewModel: TeacherProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var name by remember { mutableStateOf(userName) }
    var hourlyRate by remember { mutableStateOf("") }
    var aboutMe by remember { mutableStateOf("") }
    var isListed by remember { mutableStateOf(false) }
    var teachingLanguages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var instructionLanguages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var backClicked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(profile) {
        if (profile != null) {
            name = profile!!.name
            hourlyRate = profile!!.hourlyRate.toString()
            aboutMe = profile!!.aboutMe
            isListed = profile!!.isListed
            teachingLanguages = profile!!.teachingLanguages.toSet()
            instructionLanguages = profile!!.instructionLanguages.toSet()
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveSuccess()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Your Teacher Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = hourlyRate,
                        onValueChange = { hourlyRate = it },
                        label = { Text("Hourly Rate (£)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = aboutMe,
                        onValueChange = { aboutMe = it },
                        label = { Text("About Me") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )

                    // ─── Languages I Teach ────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Languages I Teach",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Select the languages you can teach students",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        // Split into rows of 2 chips each
                        AppLanguage.all.chunked(2).forEach { rowLangs ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowLangs.forEach { lang ->
                                    val selected = lang.code in teachingLanguages
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            teachingLanguages = if (selected) {
                                                teachingLanguages - lang.code
                                            } else {
                                                teachingLanguages + lang.code
                                            }
                                        },
                                        label = { Text(lang.displayName) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // If odd number, fill remaining space
                                if (rowLangs.size < 2) {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // ─── Lesson Instruction Languages ─────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Lesson Language",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Select the languages you can conduct lessons in",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        AppLanguage.all.chunked(2).forEach { rowLangs ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowLangs.forEach { lang ->
                                    val selected = lang.code in instructionLanguages
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            instructionLanguages = if (selected) {
                                                instructionLanguages - lang.code
                                            } else {
                                                instructionLanguages + lang.code
                                            }
                                        },
                                        label = { Text(lang.displayName) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowLangs.size < 2) {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // ─── Listed toggle ────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "List my profile",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isListed) "Visible to students"
                                       else "Hidden from students",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isListed) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = isListed,
                            onCheckedChange = { isListed = it }
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = {
                            val rate = hourlyRate.toDoubleOrNull() ?: 0.0
                            viewModel.saveProfile(
                                userId = userId,
                                name = name,
                                hourlyRate = rate,
                                aboutMe = aboutMe,
                                isListed = isListed,
                                teachingLanguages = teachingLanguages.toList(),
                                instructionLanguages = instructionLanguages.toList()
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank() &&
                                hourlyRate.isNotBlank() &&
                                aboutMe.isNotBlank()
                    ) {
                        Text(
                            text = "Save Profile",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
