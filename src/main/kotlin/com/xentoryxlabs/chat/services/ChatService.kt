package com.xentoryxlabs.chat.services

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import com.xentoryxlabs.chat.models.Message
import com.xentoryxlabs.chat.repositories.ChatRepository
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class ChatService(
    private val chatRepository: ChatRepository
) {
    // In-memory registry mapping userId to active WebSocket session
    private val activeSessions = ConcurrentHashMap<String, WebSocketServerSession>()

    fun registerSession(userId: String, session: WebSocketServerSession) {
        activeSessions[userId] = session
        println("User $userId connected to WebSocket.")
    }

    fun removeSession(userId: String) {
        activeSessions.remove(userId)
        println("User $userId disconnected from WebSocket.")
    }

    suspend fun sendMessageToUser(userId: String, message: Message) {
        val session = activeSessions[userId]
        if (session != null) {
            try {
                val jsonMessage = Json.encodeToString(Message.serializer(), message)
                session.send(Frame.Text(jsonMessage))
            } catch (e: Exception) {
                println("Failed to send message to user $userId: ${e.localizedMessage}")
                removeSession(userId) // clean up bad session
            }
        }
    }

    fun getActiveSessions(): Map<String, WebSocketServerSession> {
        return activeSessions
    }
}
