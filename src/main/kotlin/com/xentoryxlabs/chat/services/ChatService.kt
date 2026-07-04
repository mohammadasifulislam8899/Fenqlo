package com.xentoryxlabs.chat.services

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import com.xentoryxlabs.chat.models.Message
import com.xentoryxlabs.chat.repositories.ChatRepository
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub

class ChatService(
    private val chatRepository: ChatRepository,
    private val redisPool: JedisPool
) {
    // In-memory registry mapping userId to active WebSocket session on this local server instance
    private val activeSessions = ConcurrentHashMap<String, WebSocketServerSession>()

    init {
        // Start background subscriber coroutine to listen for broadcasted messages across all nodes
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    redisPool.resource.use { jedis ->
                        println("Subscribing to Redis channel chat:messages...")
                        jedis.subscribe(object : JedisPubSub() {
                            override fun onMessage(channel: String, messageText: String) {
                                try {
                                    val message = Json.decodeFromString(Message.serializer(), messageText)
                                    // Send to local active sessions of conversation members
                                    launch {
                                        val conversation = chatRepository.findConversationById(message.conversationId)
                                        conversation?.members?.forEach { memberId ->
                                            sendLocalMessage(memberId, message)
                                        }
                                    }
                                } catch (e: Exception) {
                                    println("Error parsing pub/sub message: ${e.localizedMessage}")
                                }
                            }
                        }, "chat:messages")
                    }
                } catch (e: Exception) {
                    println("Redis subscription disconnected or failed, retrying in 5 seconds... Error: ${e.localizedMessage}")
                    delay(5000)
                }
            }
        }
    }

    fun registerSession(userId: String, session: WebSocketServerSession) {
        activeSessions[userId] = session
        println("User $userId connected to WebSocket.")
    }

    fun removeSession(userId: String) {
        activeSessions.remove(userId)
        println("User $userId disconnected from WebSocket.")
    }

    /**
     * Publishes a message to the Redis channel to broadcast to all server instances.
     */
    suspend fun publishMessage(message: Message) = withContext(Dispatchers.IO) {
        try {
            redisPool.resource.use { jedis ->
                val jsonMessage = Json.encodeToString(Message.serializer(), message)
                jedis.publish("chat:messages", jsonMessage)
            }
        } catch (e: Exception) {
            println("Failed to publish message to Redis: ${e.localizedMessage}")
        }
    }

    /**
     * Sends a message directly through the local WebSocket session if the user is connected to this instance.
     */
    private suspend fun sendLocalMessage(userId: String, message: Message) {
        val session = activeSessions[userId]
        if (session != null) {
            try {
                val jsonMessage = Json.encodeToString(Message.serializer(), message)
                session.send(Frame.Text(jsonMessage))
            } catch (e: Exception) {
                println("Failed to send local message to user $userId: ${e.localizedMessage}")
                removeSession(userId)
            }
        }
    }

    /**
     * Caches user status as online in Redis.
     */
    suspend fun setUserOnline(userId: String) = withContext(Dispatchers.IO) {
        try {
            redisPool.resource.use { jedis ->
                jedis.set("user:status:$userId", "online")
            }
        } catch (e: Exception) {
            println("Failed to cache online status in Redis: ${e.localizedMessage}")
        }
    }

    /**
     * Caches user status as offline in Redis.
     */
    suspend fun setUserOffline(userId: String) = withContext(Dispatchers.IO) {
        try {
            redisPool.resource.use { jedis ->
                jedis.set("user:status:$userId", "offline")
            }
        } catch (e: Exception) {
            println("Failed to cache offline status in Redis: ${e.localizedMessage}")
        }
    }

    fun getActiveSessions(): Map<String, WebSocketServerSession> {
        return activeSessions
    }
}
