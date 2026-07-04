package com.xentoryxlabs.chat.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.xentoryxlabs.chat.models.Conversation
import com.xentoryxlabs.chat.models.Message
import com.xentoryxlabs.chat.requests.CreateConversationRequest
import com.xentoryxlabs.chat.repositories.ChatRepository
import com.xentoryxlabs.chat.services.ChatService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import java.util.*

@Serializable
data class IncomingWsMessage(
    val conversationId: String,
    val content: String
)

fun Route.chatRoutes() {
    val chatService by inject<ChatService>()
    val chatRepository by inject<ChatRepository>()

    // Public WebSocket endpoint with manual JWT query parameter authentication
    webSocket("/api/chat/ws") {
        val token = call.request.queryParameters["token"]
        if (token == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Token missing"))
            return@webSocket
        }

        val jwtSecret = "secret"
        val jwtIssuer = "https://jwt-provider-domain/"
        val jwtAudience = "jwt-audience"

        val verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .build()

        val decodedJWT = try {
            verifier.verify(token)
        } catch (e: Exception) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
            return@webSocket
        }

        val userId = decodedJWT.getClaim("userId").asString()
        if (userId == null || userId.isEmpty()) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid userId claim"))
            return@webSocket
        }

        // Register session in ChatService and cache status as online
        chatService.registerSession(userId, this)
        chatService.setUserOnline(userId)

        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try {
                        val incomingMsg = Json.decodeFromString(IncomingWsMessage.serializer(), text)
                        val conversation = chatRepository.findConversationById(incomingMsg.conversationId)
                        
                        if (conversation != null && conversation.members.contains(userId)) {
                            val message = Message(
                                id = UUID.randomUUID().toString(),
                                conversationId = incomingMsg.conversationId,
                                senderId = userId,
                                content = incomingMsg.content,
                                timestamp = System.currentTimeMillis()
                            )

                            // Save message in MongoDB
                            chatRepository.saveMessage(message)

                            // Publish message to Redis Pub/Sub to broadcast to all nodes
                            chatService.publishMessage(message)
                        }
                    } catch (e: Exception) {
                        send(Frame.Text(Json.encodeToString(mapOf("error" to "Failed to process message: ${e.localizedMessage}"))))
                    }
                }
            }
        } finally {
            // Clean up session and cache status as offline on disconnect
            chatService.removeSession(userId)
            chatService.setUserOffline(userId)
        }
    }

    // Protected HTTP REST endpoints
    authenticate("auth-jwt") {
        route("/api/chat") {
            post("/conversations") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                
                val request = call.receive<CreateConversationRequest>()
                
                // Ensure requesting user is included in members
                val membersList = if (!request.members.contains(userId)) {
                    request.members + userId
                } else {
                    request.members
                }

                val conversation = Conversation(
                    id = UUID.randomUUID().toString(),
                    type = request.type,
                    name = request.name,
                    members = membersList,
                    createdBy = userId,
                    createdAt = System.currentTimeMillis()
                )

                val success = chatRepository.createConversation(conversation)
                if (success) {
                    call.respond(HttpStatusCode.Created, conversation)
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to create conversation"))
                }
            }

            get("/conversations") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString() ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val conversations = chatRepository.findConversationsForUser(userId)
                call.respond(HttpStatusCode.OK, conversations)
            }

            get("/conversations/{id}/messages") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                val conversationId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing conversation ID"))

                // Verify user is member of conversation before loading messages
                val conversation = chatRepository.findConversationById(conversationId)
                if (conversation == null || !conversation.members.contains(userId)) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied to conversation"))
                    return@get
                }

                val messages = chatRepository.findMessagesForConversation(conversationId)
                call.respond(HttpStatusCode.OK, messages)
            }
        }
    }
}
