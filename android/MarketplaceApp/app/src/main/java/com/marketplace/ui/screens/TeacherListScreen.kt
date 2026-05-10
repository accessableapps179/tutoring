// app/src/main/java/com/marketplace/ui/screens/TeacherListScreen.kt
package com.marketplace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.MarketplaceFirebaseService
import com.marketplace.Session
import com.marketplace.dto.AppLanguage
import com.marketplace.dto.TeacherDto
import com.marketplace.viewmodel.MessageViewModel
import com.marketplace.viewmodel.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TeacherListScreen(
    role: String = "",
    userName: String = "",
    onTeacherClick: (TeacherDto) -> Unit,
    onMyBookingsClick: () -> Unit = {},
    onManageProfileClick: () -> Unit = {},
    onManageAvailabilityClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onPaymentCardClick: () -> Unit = {},
    onBalanceClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    viewModel: TeacherViewModel = viewModel(),
    messageViewModel: MessageViewModel
) {
    val teachers by viewModel.teachers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val unreadCount by messageViewModel.unreadCount.collectAsState()
    val context = LocalContext.current

    var selectedTargetLanguage by remember { mutableStateOf<AppLanguage?>(null) }
    var selectedInstructionLanguage by remember { mutableStateOf<AppLanguage?>(null) }
    var targetDropdownExpanded by remember { mutableStateOf(false) }
    var instructionDropdownExpanded by remember { mutableStateOf(false) }

    // Keep Session search results in sync whenever teachers list updates after a search
    LaunchedEffect(teachers) {
        if (isSearchActive && teachers.isNotEmpty()) {
            Session.lastSearchResults = teachers
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            if (role == "STUDENT") {
                if (isSearchActive) {
                    selectedTargetLanguage?.let { target ->
                        viewModel.searchTeachers(target.code, selectedInstructionLanguage?.code)
                    }
                }
            } else {
                viewModel.loadTeachers()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (role != "STUDENT") {
            viewModel.loadTeachers()
        }
        messageViewModel.loadUnreadCount()
    }

    // Fix #3: start polling in the ViewModel (which owns viewModelScope and can use delay
    // safely), and stop it when this screen leaves composition via DisposableEffect.
    DisposableEffect(Unit) {
        messageViewModel.startUnreadPolling()
        onDispose { messageViewModel.stopUnreadPolling() }
    }

    LaunchedEffect(unreadCount) {
        MarketplaceFirebaseService.setBadgeCount(context, unreadCount)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Welcome, $userName",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(onClick = onMessagesClick) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge {
                                        Text(text = if (unreadCount > 99) "99+" else unreadCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ChatBubble,
                                contentDescription = "Messages",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
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
                .pullRefresh(pullRefreshState)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                        Button(
                            onClick = onMessagesClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(
                                text = if (unreadCount > 0) "💬 Messages ($unreadCount unread)"
                                else "💬 Messages",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (role == "TEACHER") {
                            Button(onClick = onManageProfileClick, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Manage My Profile", fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = onManageAvailabilityClick, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Manage My Availability", fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = onBalanceClick, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "💰 My Balance", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (role == "STUDENT") {
                            Button(onClick = onMyBookingsClick, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "My Bookings", fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = onPaymentCardClick, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Payment Card", fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = onBalanceClick, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "My Account", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onChangePasswordClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(text = "Change Password", fontWeight = FontWeight.Bold)
                        }

                        if (role == "STUDENT") {
                            Text(
                                text = "Search Teachers by Language",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
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

                            Button(
                                onClick = {
                                    selectedTargetLanguage?.let { target ->
                                        Session.lastSearchTargetLanguage      = target.code
                                        Session.lastSearchInstructionLanguage = selectedInstructionLanguage?.code
                                        viewModel.searchTeachers(target.code, selectedInstructionLanguage?.code)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = selectedTargetLanguage != null
                            ) {
                                Text(text = "Search", fontWeight = FontWeight.Bold)
                            }

                            if (isSearchActive) {
                                OutlinedButton(
                                    onClick = {
                                        selectedTargetLanguage = null
                                        selectedInstructionLanguage = null
                                        viewModel.clearSearch()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Clear Search")
                                }
                            }
                        }
                    }
                }

                items(teachers) { teacher ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}