// Android Studio
// app/src/main/java/com/marketplace/ui/screens/MessagesListScreen.kt
package com.marketplace.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Badge
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marketplace.MarketplaceFirebaseService
import com.marketplace.dto.ContactDto
import com.marketplace.viewmodel.ContactViewModel
import com.marketplace.viewmodel.MessageViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesListScreen(
    role: String,
    userId: String,
    onBackClick: () -> Unit,
    onChatClick: (contactId: String, otherPersonName: String) -> Unit,
    contactViewModel: ContactViewModel = viewModel(),
    messageViewModel: MessageViewModel = viewModel()
) {
    val contacts by contactViewModel.contacts.collectAsState()
    val pendingContacts by contactViewModel.pendingContacts.collectAsState()
    val isLoading by contactViewModel.isLoading.collectAsState()
    val previewMessages by messageViewModel.previewMessages.collectAsState()
    val unreadCount by messageViewModel.unreadCount.collectAsState()
    val context = LocalContext.current

    var backClicked by remember { mutableStateOf(false) }

    // Keep launcher icon badge in sync while on this screen
    LaunchedEffect(unreadCount) {
        MarketplaceFirebaseService.setBadgeCount(context, unreadCount)
    }

    // Load contacts and initial previews on entry
    LaunchedEffect(Unit) {
        backClicked = false
        contactViewModel.loadContacts()
        if (role == "TEACHER") {
            contactViewModel.loadPendingContacts()
        }
        messageViewModel.loadUnreadCount()
    }

    // Once contacts load, load previews and start polling
    LaunchedEffect(contacts) {
        if (contacts.isNotEmpty()) {
            contacts.forEach { contact ->
                messageViewModel.loadMessagesPreview(contact.id)
            }
            messageViewModel.startPreviewPolling(contacts.map { it.id })
        }
    }

    // Stop polling when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            messageViewModel.stopPreviewPolling()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Messages",
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
            } else if (contacts.isEmpty() && pendingContacts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💬",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (role == "STUDENT")
                            "Add a teacher as a contact to start messaging"
                        else
                            "Students will appear here once they add you",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (role == "TEACHER" && pendingContacts.isNotEmpty()) {
                        item {
                            Text(
                                text = "Contact Requests",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                            )
                        }
                        items(pendingContacts) { contact ->
                            PendingContactItem(
                                contact = contact,
                                onAccept = { contactViewModel.acceptContact(contact.id) },
                                onDecline = { contactViewModel.declineContact(contact.id) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                        item {
                            Text(
                                text = "Conversations",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                            )
                        }
                    }

                    items(contacts) { contact ->
                        val otherPersonName = if (role == "TEACHER")
                            contact.studentName
                        else
                            contact.teacherName

                        val contactMessages = previewMessages[contact.id] ?: emptyList()
                        val lastMessage = contactMessages.maxByOrNull { it.timestamp }
                        val unreadCount = contactMessages
                            .count { !it.isRead && it.senderId != userId }

                        ConversationItem(
                            otherPersonName = otherPersonName,
                            lastMessageContent = lastMessage?.content,
                            lastMessageTimestamp = lastMessage?.timestamp,
                            unreadCount = unreadCount,
                            onClick = { onChatClick(contact.id, otherPersonName) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    otherPersonName: String,
    lastMessageContent: String?,
    lastMessageTimestamp: Long?,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = otherPersonName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = otherPersonName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = lastMessageContent ?: "No messages yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (lastMessageTimestamp != null) {
                Text(
                    text = formatTimestamp(lastMessageTimestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (unreadCount > 0) {
                Badge {
                    Text(text = unreadCount.toString())
                }
            }
        }
    }
}

@Composable
fun PendingContactItem(
    contact: ContactDto,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = contact.studentName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.studentName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Wants to connect",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAccept,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Accept",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = onDecline,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Decline",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
    }
}