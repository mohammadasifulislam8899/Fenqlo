package com.xentoryxlabs.chat.repositories

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.xentoryxlabs.chat.models.Conversation
import com.xentoryxlabs.chat.models.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings

class MongoChatRepository(
    private val database: MongoDatabase
) : ChatRepository {

    private val conversationsCollection = database.getCollection("conversations")
    private val messagesCollection = database.getCollection("messages")

    private val jsonSettings = JsonWriterSettings.builder()
        .outputMode(JsonMode.RELAXED)
        .build()

    override suspend fun createConversation(conversation: Conversation): Boolean = withContext(Dispatchers.IO) {
        val jsonString = Json.encodeToString(Conversation.serializer(), conversation)
        val document = Document.parse(jsonString)
        val result = conversationsCollection.insertOne(document)
        result.wasAcknowledged()
    }

    override suspend fun findConversationById(id: String): Conversation? = withContext(Dispatchers.IO) {
        val document = conversationsCollection.find(Filters.eq("_id", id)).first()
        document?.let {
            Json.decodeFromString(Conversation.serializer(), it.toJson(jsonSettings))
        }
    }

    override suspend fun findConversationsForUser(userId: String): List<Conversation> = withContext(Dispatchers.IO) {
        val conversations = mutableListOf<Conversation>()
        conversationsCollection.find(Filters.eq("members", userId)).forEach { doc ->
            val conversation = Json.decodeFromString(Conversation.serializer(), doc.toJson(jsonSettings))
            conversations.add(conversation)
        }
        conversations
    }

    override suspend fun saveMessage(message: Message): Boolean = withContext(Dispatchers.IO) {
        val jsonString = Json.encodeToString(Message.serializer(), message)
        val document = Document.parse(jsonString)
        val result = messagesCollection.insertOne(document)
        result.wasAcknowledged()
    }

    override suspend fun findMessagesForConversation(conversationId: String): List<Message> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<Message>()
        messagesCollection.find(Filters.eq("conversationId", conversationId))
            .sort(Sorts.ascending("timestamp"))
            .forEach { doc ->
                val message = Json.decodeFromString(Message.serializer(), doc.toJson(jsonSettings))
                messages.add(message)
            }
        messages
    }
}
