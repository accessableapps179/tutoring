// app/src/main/java/com/marketplace/ui/screens/VideoCallScreen.kt
package com.marketplace.ui.screens

import android.annotation.SuppressLint
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.marketplace.api.RetrofitClient

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    contactId: String,
    otherPersonName: String,
    role: String,
    onEndCall: () -> Unit
) {
    val roomId = contactId.take(12)
    val signalingUrl = RetrofitClient.getWebSocketUrl(roomId)

    // Fix #4: escape single quotes so a name like O'Brien doesn't break the JS string literal.
    val safeOtherName = otherPersonName.replace("'", "\\'")

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }
    var isSpeakerOff by remember { mutableStateOf(false) }

    val html = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }

  body {
    background: #000;
    display: flex;
    flex-direction: column;
    height: 100vh;
    overflow: hidden;
    position: relative;
  }

  #remoteVideo {
    width: 100%;
    flex: 1;
    object-fit: cover;
    background: #111;
  }

  #status {
    color: #aaa;
    font-family: sans-serif;
    font-size: 13px;
    text-align: center;
    padding: 8px;
    background: #000;
    flex-shrink: 0;
  }

  #pipContainer {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 110px;
    height: 150px;
    z-index: 10;
    border-radius: 12px;
    overflow: hidden;
    border: 2px solid rgba(255,255,255,0.8);
    box-shadow: 0 4px 12px rgba(0,0,0,0.5);
    touch-action: none;
    cursor: grab;
  }

  #localVideo {
    width: 100%;
    height: 100%;
    object-fit: cover;
    background: #222;
    display: block;
    pointer-events: none;
  }

  #cameraOffOverlay {
    display: none;
    width: 100%;
    height: 100%;
    background: #333;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-family: sans-serif;
    font-size: 12px;
    text-align: center;
    pointer-events: none;
  }
</style>
</head>
<body>

<video id="remoteVideo" autoplay playsinline></video>
<div id="status">Starting...</div>

<div id="pipContainer">
  <video id="localVideo" autoplay playsinline muted></video>
  <div id="cameraOffOverlay">Camera Off</div>
</div>

<script>
  const signalingUrl = '$signalingUrl';
  const otherName = '$safeOtherName';
  const status = document.getElementById('status');
  const localVideo = document.getElementById('localVideo');
  const remoteVideo = document.getElementById('remoteVideo');
  const cameraOffOverlay = document.getElementById('cameraOffOverlay');
  const pipContainer = document.getElementById('pipContainer');

  const PIP_W = 110;
  const PIP_H = 150;
  let pipLeft = null;
  let pipTop = 16;
  let dragStartX = 0;
  let dragStartY = 0;
  let dragStartLeft = 0;
  let dragStartTop = 0;
  let isDragging = false;

  function initPipPosition() {
    pipLeft = window.innerWidth - PIP_W - 16;
    pipTop = 16;
    pipContainer.style.right = 'auto';
    pipContainer.style.left = pipLeft + 'px';
    pipContainer.style.top = pipTop + 'px';
  }

  function clamp(val, min, max) {
    return Math.max(min, Math.min(max, val));
  }

  pipContainer.addEventListener('touchstart', (e) => {
    if (pipLeft === null) initPipPosition();
    isDragging = true;
    dragStartX = e.touches[0].clientX;
    dragStartY = e.touches[0].clientY;
    dragStartLeft = pipLeft;
    dragStartTop = pipTop;
    pipContainer.style.cursor = 'grabbing';
    e.preventDefault();
  }, { passive: false });

  pipContainer.addEventListener('touchmove', (e) => {
    if (!isDragging) return;
    const dx = e.touches[0].clientX - dragStartX;
    const dy = e.touches[0].clientY - dragStartY;
    pipLeft = clamp(dragStartLeft + dx, 4, window.innerWidth - PIP_W - 4);
    pipTop = clamp(dragStartTop + dy, 4, window.innerHeight - PIP_H - 4);
    pipContainer.style.left = pipLeft + 'px';
    pipContainer.style.top = pipTop + 'px';
    e.preventDefault();
  }, { passive: false });

  pipContainer.addEventListener('touchend', () => {
    isDragging = false;
    pipContainer.style.cursor = 'grab';
  });

  function setMuted(muted) {
    if (localStream) {
      localStream.getAudioTracks().forEach(t => { t.enabled = !muted; });
    }
  }

  function setCameraOff(off) {
    if (localStream) {
      localStream.getVideoTracks().forEach(t => { t.enabled = !off; });
      if (off) {
        localVideo.style.display = 'none';
        cameraOffOverlay.style.display = 'flex';
      } else {
        localVideo.style.display = 'block';
        cameraOffOverlay.style.display = 'none';
      }
    }
  }

  function setSpeakerOff(off) {
    remoteVideo.muted = off;
  }

  const config = { iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] };
  let pc = null;
  let ws = null;
  let localStream = null;
  let myRole = null;
  let intentionalClose = false;
  let reconnectScheduled = false;

  async function start() {
    try {
      localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
      localVideo.srcObject = localStream;
      status.textContent = 'Camera ready. Connecting...';
      connectWs();
    } catch(e) {
      status.textContent = 'Camera error: ' + e.message;
    }
  }

  function connectWs() {
    intentionalClose = false;
    reconnectScheduled = false;
    myRole = null;

    if (ws) {
      ws.onclose = null;
      ws.onerror = null;
      ws.onmessage = null;
      ws.close();
      ws = null;
    }
    closePeer();
    remoteVideo.srcObject = null;

    ws = new WebSocket(signalingUrl);

    ws.onopen = () => {
      console.log('Call WS open');
      status.textContent = 'Waiting for other person...';
    };

    ws.onmessage = async (event) => {
      const msg = JSON.parse(event.data);
      console.log('Call received: ' + msg.type + (msg.role ? ' role=' + msg.role : ''));

      if (msg.type === 'waiting_for_peer') {
        myRole = msg.role;
        status.textContent = 'Waiting for ' + otherName + ' to rejoin...';

      } else if (msg.type === 'ready') {
        myRole = msg.role;
        console.log('Both peers ready, my role: ' + myRole);
        if (myRole === 'initiator') {
          status.textContent = 'Creating offer...';
          await createPeerAndOffer();
        } else {
          status.textContent = 'Waiting for offer...';
          createPeer();
        }

      } else if (msg.type === 'offer') {
        status.textContent = 'Received offer, sending answer...';
        if (!pc) { createPeer(); }
        await pc.setRemoteDescription(
          new RTCSessionDescription({ type: 'offer', sdp: msg.sdp })
        );
        const answer = await pc.createAnswer();
        await pc.setLocalDescription(answer);
        ws.send(JSON.stringify({ type: 'answer', sdp: answer.sdp }));

      } else if (msg.type === 'answer') {
        await pc.setRemoteDescription(
          new RTCSessionDescription({ type: 'answer', sdp: msg.sdp })
        );
        status.textContent = 'Connected!';

      } else if (msg.type === 'ice') {
        if (pc) {
          try {
            await pc.addIceCandidate(new RTCIceCandidate({
              candidate: msg.candidate,
              sdpMid: msg.sdpMid,
              sdpMLineIndex: msg.sdpMLineIndex
            }));
          } catch(e) { console.error('ICE error:', e); }
        }

      } else if (msg.type === 'peer_left') {
        console.log('Peer left — reconnecting WebSocket to wait for rejoin');
        status.textContent = otherName + ' left. Waiting for rejoin...';
        remoteVideo.srcObject = null;
        closePeer();
        scheduleReconnect();
      }
    };

    ws.onerror = (e) => {
      console.log('Call WS error: ' + e);
      status.textContent = 'Connection error...';
    };

    ws.onclose = () => {
      console.log('Call WS closed — intentional: ' + intentionalClose);
      if (!intentionalClose && !reconnectScheduled) {
        scheduleReconnect();
      }
    };
  }

  function scheduleReconnect() {
    if (reconnectScheduled) return;
    reconnectScheduled = true;
    setTimeout(() => {
      if (!intentionalClose) {
        console.log('Reconnecting...');
        connectWs();
      }
    }, 2000);
  }

  function closePeer() {
    if (pc) { pc.close(); pc = null; }
  }

  function createPeer() {
    closePeer();
    pc = new RTCPeerConnection(config);
    localStream.getTracks().forEach(track => pc.addTrack(track, localStream));

    pc.onicecandidate = (e) => {
      if (e.candidate && ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
          type: 'ice',
          candidate: e.candidate.candidate,
          sdpMid: e.candidate.sdpMid,
          sdpMLineIndex: e.candidate.sdpMLineIndex
        }));
      }
    };

    pc.ontrack = (e) => {
      remoteVideo.srcObject = e.streams[0];
      status.textContent = 'In call';
    };

    pc.onconnectionstatechange = () => {
      console.log('Connection state: ' + pc.connectionState);
      if (pc.connectionState === 'connected') status.textContent = 'In call';
      else if (pc.connectionState === 'failed') status.textContent = 'Connection failed';
      else if (pc.connectionState === 'disconnected') status.textContent = 'Connection lost...';
    };
  }

  async function createPeerAndOffer() {
    createPeer();
    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);
    ws.send(JSON.stringify({ type: 'offer', sdp: offer.sdp }));
    status.textContent = 'Offer sent, waiting for answer...';
  }

  start();
</script>
</body>
</html>
    """.trimIndent()

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                evaluateJavascript("intentionalClose = true;", null)
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
                        text = "Call with $otherPersonName",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = true
                            allowContentAccess = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                        }
                        webViewClient = WebViewClient()
                        webChromeClient = object : WebChromeClient() {
                            override fun onPermissionRequest(request: PermissionRequest) {
                                request.grant(request.resources)
                            }
                        }
                        webViewRef = this
                        loadDataWithBaseURL("http://localhost", html, "text/html", "UTF-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = {
                        isSpeakerOff = !isSpeakerOff
                        webViewRef?.evaluateJavascript("setSpeakerOff($isSpeakerOff);", null)
                    },
                    containerColor = if (isSpeakerOff) Color.White else Color.DarkGray
                ) {
                    Icon(
                        imageVector = if (isSpeakerOff) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                        contentDescription = if (isSpeakerOff) "Speaker off" else "Speaker on",
                        tint = if (isSpeakerOff) Color.Red else Color.White
                    )
                }

                FloatingActionButton(
                    onClick = {
                        isMuted = !isMuted
                        webViewRef?.evaluateJavascript("setMuted($isMuted);", null)
                    },
                    containerColor = if (isMuted) Color.White else Color.DarkGray
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = if (isMuted) Color.Red else Color.White
                    )
                }

                FloatingActionButton(
                    onClick = onEndCall,
                    containerColor = Color.Red
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "End call",
                        tint = Color.White
                    )
                }

                FloatingActionButton(
                    onClick = {
                        isCameraOff = !isCameraOff
                        webViewRef?.evaluateJavascript("setCameraOff($isCameraOff);", null)
                    },
                    containerColor = if (isCameraOff) Color.White else Color.DarkGray
                ) {
                    Icon(
                        imageVector = if (isCameraOff) Icons.Filled.VideocamOff else Icons.Filled.Videocam,
                        contentDescription = if (isCameraOff) "Camera on" else "Camera off",
                        tint = if (isCameraOff) Color.Red else Color.White
                    )
                }
            }
        }
    }
}