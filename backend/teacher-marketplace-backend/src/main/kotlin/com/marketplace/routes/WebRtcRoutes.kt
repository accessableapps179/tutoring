// src/main/kotlin/com/marketplace/routes/WebRtcRoutes.kt
package com.marketplace.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class CallStatusResponse(val exists: Boolean, val peerCount: Int)

data class RoomPeer(val session: WebSocketSession, val joinedAt: Long)

val lobbyRooms = ConcurrentHashMap<String, MutableList<RoomPeer>>()
val callRooms  = ConcurrentHashMap<String, MutableList<RoomPeer>>()

@OptIn(DelicateCoroutinesApi::class)
fun Application.webRtcRoutes() {
    routing {

        // ─── HTTP endpoint — check if a call room exists ─────────────────────
        // Client calls this before deciding lobby vs direct rejoin
        get("/call-status/{roomId}") {
            val roomId = call.parameters["roomId"] ?: return@get call.respond(
                HttpStatusCode.BadRequest, "Missing room ID"
            )
            val peers = callRooms[roomId]
            val peerCount = peers?.size ?: 0
            call.respond(CallStatusResponse(exists = peerCount > 0, peerCount = peerCount))
        }

        // ─── Lobby endpoint ──────────────────────────────────────────────────
        webSocket("/lobby/{roomId}") {
            val roomId = call.parameters["roomId"] ?: run {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing room ID"))
                return@webSocket
            }

            val peers = lobbyRooms.getOrPut(roomId) { mutableListOf() }
            val thisPeer = RoomPeer(session = this, joinedAt = System.currentTimeMillis())
            peers.add(thisPeer)

            val peerCount = peers.size
            application.log.info("Lobby: peer joined room $roomId, total: $peerCount")

            when (peerCount) {
                1 -> {
                    send("""{"type":"waiting"}""")
                }
                2 -> {
                    peers.forEach { peer ->
                        peer.session.send("""{"type":"peer_arrived"}""")
                    }
                }
                else -> {
                    send("""{"type":"room_full"}""")
                    peers.remove(thisPeer)
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room is full"))
                    return@webSocket
                }
            }

            try {
                for (frame in incoming) {
                    // Lobby only gates entry — no message relay
                }
            } finally {
                peers.removeAll { it.session === this }
                if (peers.isEmpty()) {
                    lobbyRooms.remove(roomId)
                }
                peers.forEach { peer ->
                    try {
                        peer.session.send("""{"type":"peer_left"}""")
                    } catch (e: Exception) { }
                }
                application.log.info("Lobby: peer left room $roomId, remaining: ${peers.size}")
            }
        }

        // ─── Call endpoint ───────────────────────────────────────────────────
        webSocket("/signal/{roomId}") {
            val roomId = call.parameters["roomId"] ?: run {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing room ID"))
                return@webSocket
            }

            val peers = callRooms.getOrPut(roomId) { mutableListOf() }
            val thisPeer = RoomPeer(session = this, joinedAt = System.currentTimeMillis())
            peers.add(thisPeer)

            val peerCount = peers.size
            application.log.info("Call: peer joined room $roomId, total: $peerCount")

            when (peerCount) {
                1 -> {
                    // First peer — wait for the other to join or rejoin
                    send("""{"type":"waiting_for_peer","role":"answerer"}""")
                }
                2 -> {
                    // Both peers present — initiate the WebRTC handshake
                    val firstPeer = peers[0]
                    firstPeer.session.send("""{"type":"ready","role":"answerer"}""")
                    send("""{"type":"ready","role":"initiator"}""")
                }
                else -> {
                    // More than 2 — stale peers from a previous connection, clear them out
                    application.log.info("Call: room $roomId has stale peers, clearing")
                    val stalePeers = peers.filter { it.session !== this }
                    stalePeers.forEach { peer ->
                        try {
                            peer.session.send("""{"type":"peer_left"}""")
                            peer.session.close(CloseReason(CloseReason.Codes.NORMAL, "Room reset"))
                        } catch (e: Exception) { }
                    }
                    peers.clear()
                    peers.add(thisPeer)
                    send("""{"type":"waiting_for_peer","role":"answerer"}""")
                }
            }

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val message = frame.readText()
                        application.log.info("Call relay [$roomId]: ${message.take(60)}")
                        // Relay all signaling messages to the other peer
                        peers.forEach { peer ->
                            if (peer.session !== this) {
                                peer.session.send(message)
                            }
                        }
                    }
                }
            } finally {
                peers.removeAll { it.session === this }
                application.log.info("Call: peer left room $roomId, remaining: ${peers.size}")

                // Fix: notify the remaining peer that their partner left, but DO NOT
                // close their WebSocket. Closing it was the bug — it forced "s" off the
                // signal room, so when "?" tried to rejoin they could never find "s" again.
                // Now "s" stays connected and waiting. When "?" reconnects to /signal,
                // peerCount hits 2 and the handshake restarts cleanly.
                val remainingPeers = peers.toList()
                remainingPeers.forEach { peer ->
                    try {
                        peer.session.send("""{"type":"peer_left"}""")
                    } catch (e: Exception) { }
                }

                // Clean up the room after 60s if still empty (both left)
                if (peers.isEmpty()) {
                    application.log.info("Call: room $roomId empty, scheduling cleanup")
                    GlobalScope.launch {
                        delay(60_000)
                        val currentPeers = callRooms[roomId]
                        if (currentPeers != null && currentPeers.isEmpty()) {
                            callRooms.remove(roomId)
                            application.log.info("Call: room $roomId cleaned up after grace period")
                        }
                    }
                }
            }
        }
    }
}