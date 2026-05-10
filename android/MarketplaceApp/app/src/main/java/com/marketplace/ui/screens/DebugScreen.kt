// Android Studio
// app/src/main/java/com/marketplace/ui/screens/DebugScreen.kt
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marketplace.dto.DebugUserDto
import com.marketplace.repository.AuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onBackClick: () -> Unit,
    onNukeComplete: () -> Unit
) {
    val repository = remember { AuthRepository() }
    var users by remember { mutableStateOf<List<DebugUserDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isNuking by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showNukeDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = repository.getDebugUsers()
        if (result.isSuccess) {
            users = result.getOrElse { emptyList() }
        } else {
            error = "Failed to load users — is the backend running?"
        }
        isLoading = false
    }

    if (showNukeDialog) {
        AlertDialog(
            onDismissRequest = { showNukeDialog = false },
            title = {
                Text(
                    text = "☢️ NUKE DATABASE",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("This will drop and recreate all tables. All data will be permanently deleted. Are you sure?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNukeDialog = false
                        isNuking = true
                        scope.launch {
                            val result = repository.nukeDatabase()
                            if (result.isSuccess) {
                                onNukeComplete()
                            } else {
                                error = "Nuke failed — check backend"
                                isNuking = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("YES, NUKE IT", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNukeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🛠 Debug — All Users",
                        fontWeight = FontWeight.Bold
                    )
                },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isNuking -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("☢️", fontSize = 52.sp)
                        CircularProgressIndicator()
                        Text(
                            text = "Nuking database...",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                error != null -> Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                    ) {
                        item {
                            Button(
                                onClick = { showNukeDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                )
                            ) {
                                Text(
                                    text = "☁️ NUKE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }

                        val teachers = users.filter { it.role == "TEACHER" }
                        val students = users.filter { it.role == "STUDENT" }

                        if (teachers.isNotEmpty()) {
                            item {
                                Text(
                                    text = "TEACHERS",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(teachers) { user ->
                                DebugUserCard(user)
                            }
                        }

                        if (students.isNotEmpty()) {
                            item {
                                Text(
                                    text = "STUDENTS",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }
                            items(students) { user ->
                                DebugUserCard(user)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DebugUserCard(user: DebugUserDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = user.role,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = user.id,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}