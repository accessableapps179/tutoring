package com.marketplace.ui.screens

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.marketplace.api.RetrofitClient

sealed class LobbyState {
    object Connecting : LobbyState()
    object Waiting : LobbyState()
    object PeerArrived : LobbyState()
    object RoomFull : LobbyState()
    object Error : LobbyState()
}

class LobbyBridge(
    private val onState: (LobbyState) -> Unit,
    private val onReady: () -> Unit
) {
    @JavascriptInterface
    fun onWaiting() {
        onState(LobbyState.Waiting)
    }

    @JavascriptInterface
    fun onPeerArrived() {
        onState(LobbyState.PeerArrived)
    }

    @JavascriptInterface
    fun onRoomFull() {
        onState(LobbyState.RoomFull)
    }

    @JavascriptInterface
    fun onPeerLeft() {
        onState(LobbyState.Waiting)
    }

    @JavascriptInterface
    fun onError() {
        onState(LobbyState.Error)
    }

    @JavascriptInterface
    fun onReadyToCall() {
        onReady()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    contactId: String,
    otherPersonName: String,
    onBackClick: () -> Unit,
    onCallReady: () -> Unit
) {
    val roomId = contactId.take(12)
    val lobbyUrl = RetrofitClient.getLobbyUrl(roomId)

    var lobbyState by remember { mutableStateOf<LobbyState>(LobbyState.Connecting) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var readyToNavigate by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    if (readyToNavigate) {
        readyToNavigate = false
        onCallReady()
    }

    val html = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body>
<script>
  const lobbyUrl = '$lobbyUrl';
  let ws = null;

  function connect() {
    ws = new WebSocket(lobbyUrl);

    ws.onopen = () => {
      console.log('Lobby WS open');
    };

    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data);
        console.log('Lobby received: ' + JSON.stringify(msg));

        if (msg.type === 'waiting') {
          Android.onWaiting();
        } else if (msg.type === 'peer_arrived') {
          Android.onPeerArrived();
          // Small delay so user sees the green screen
          setTimeout(() => {
            Android.onReadyToCall();
          }, 800);
        } else if (msg.type === 'room_full') {
          Android.onRoomFull();
        } else if (msg.type === 'peer_left') {
          Android.onPeerLeft();
        }
      } catch(e) {
        Android.onError();
      }
    };

    ws.onerror = () => {
      Android.onError();
    };

    ws.onclose = () => {
      console.log('Lobby WS closed');
    };
  }

  function closeWs() {
    if (ws) {
      ws.close();
    }
  }

  connect();
</script>
</body>
</html>
    """.trimIndent()

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                loadUrl("javascript:closeWs()")
                loadUrl("about:blank")
                destroy()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Video Call",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        webViewRef?.loadUrl("javascript:closeWs()")
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Hidden WebView handles the WebSocket connection
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                        }
                        webViewClient = WebViewClient()
                        webChromeClient = object : WebChromeClient() {
                            override fun onPermissionRequest(request: PermissionRequest) {
                                request.grant(request.resources)
                            }
                        }
                        addJavascriptInterface(
                            LobbyBridge(
                                onState = { state ->
                                    lobbyState = state
                                },
                                onReady = {
                                    readyToNavigate = true
                                }
                            ),
                            "Android"
                        )
                        webViewRef = this
                        loadDataWithBaseURL(
                            "http://localhost",
                            html,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                },
                modifier = Modifier.size(1.dp)
            )

            // UI layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (lobbyState) {
                    is LobbyState.Connecting -> {
                        PulsingIcon(scale = scale)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Connecting...",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    is LobbyState.Waiting -> {
                        PulsingIcon(scale = scale)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Waiting for",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = otherPersonName,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "to join the call...",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "They will see a notification\nwhen you are waiting",
                            color = Color.Gray.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    is LobbyState.PeerArrived -> {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    color = Color(0xFF4CAF50),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VideoCall,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "$otherPersonName is here!",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Starting call...",
                            color = Color(0xFF4CAF50),
                            fontSize = 16.sp
                        )
                    }

                    is LobbyState.RoomFull -> {
                        Text(text = "⚠️", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Call in progress",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This call already has two participants",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray
                            )
                        ) {
                            Text("Go Back", color = Color.White)
                        }
                    }

                    is LobbyState.Error -> {
                        Text(text = "❌", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Connection failed",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Could not connect to the server",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray
                            )
                        ) {
                            Text("Go Back", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsingIcon(scale: Float) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .background(
                color = Color(0xFF1565C0),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.VideoCall,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(52.dp)
        )
    }
}