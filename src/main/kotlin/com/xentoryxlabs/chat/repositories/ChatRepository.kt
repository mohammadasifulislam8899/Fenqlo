package com.xentoryxlabs.chat.repositories

import com.xentoryxlabs.chat.models.Conversation
import com.xentoryxlabs.chat.models.Message

interface ChatRepository {
    suspend fun createConversation(conversation: Conversation): Boolean
    suspend fun findConversationById(id: String): Conversation?
    suspend fun findConversationsForUser(userId: String): List<Conversation>
    suspend fun saveMessage(message: Message): Boolean
    suspend fun findMessagesForConversation(conversationId: String): List<Message>
}
