// Android Studio
// app/src/main/java/com/marketplace/ui/screens/ChatScreen.kt
package com.marketplace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.MarketplaceFirebaseService
import com.marketplace.dto.MessageDto
import com.marketplace.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contactId: String,
    otherPersonName: String,
    currentUserId: String,
    onBackClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onVideoCallRejoin: () -> Unit,
    messageViewModel: MessageViewModel = viewModel()
) {
    val messages by messageViewModel.messages.collectAsState()
    val isLoading by messageViewModel.isLoading.collectAsState()
    val errorMessage by messageViewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(contactId) {
        messageViewModel.loadMessages(contactId)
        messageViewModel.startPolling(contactId)
    }

    LaunchedEffect(Unit) {
        val notificationManager =
            context.getSystemService(android.app.NotificationManager::class.java)
        notificationManager.cancelAll()
        MarketplaceFirebaseService.setBadgeCount(context, 0)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            messageViewModel.stopPolling()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.padding(vertical = 6.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        ) {
                            Box(
                                modifier = Modifier.padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = otherPersonName.firstOrNull()?.uppercase() ?: "?",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Text(
                            text = otherPersonName,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type a message...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (messageText.isNotBlank()) {
                                    messageViewModel.sendMessage(contactId, messageText.trim())
                                    messageText = ""
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                messageViewModel.sendMessage(contactId, messageText.trim())
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(
                                if (messageText.isNotBlank())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (messageText.isNotBlank())
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            if (isLoading && messages.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (messages.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "👋", style = MaterialTheme.typography.displaySmall)
                    Text(
                        text = "Say hello to $otherPersonName!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(
                            message = message,
                            isOwnMessage = message.senderId == currentUserId
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageDto,
    isOwnMessage: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isOwnMessage) 18.dp else 4.dp,
                        bottomEnd = if (isOwnMessage) 4.dp else 18.dp
                    )
                )
                .background(
                    if (isOwnMessage)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surface
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = message.content,
                    color = if (isOwnMessage)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatTimestamp(message.timestamp),
                    color = if (isOwnMessage)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}