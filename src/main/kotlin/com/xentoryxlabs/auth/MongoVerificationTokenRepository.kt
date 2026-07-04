package com.xentoryxlabs.auth

import com.xentoryxlabs.auth.models.VerificationToken

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings

class MongoVerificationTokenRepository(
    private val database: MongoDatabase
) : VerificationTokenRepository {

    private val tokensCollection = database.getCollection("verification_tokens")

    private val jsonSettings = JsonWriterSettings.builder()
        .outputMode(JsonMode.RELAXED)
        .build()

    override suspend fun create(token: VerificationToken): Boolean = withContext(Dispatchers.IO) {
        val jsonString = Json.encodeToString(VerificationToken.serializer(), token)
        val document = Document.parse(jsonString)
        val result = tokensCollection.insertOne(document)
        result.wasAcknowledged()
    }

    override suspend fun findByCode(code: String): VerificationToken? = withContext(Dispatchers.IO) {
        val document = tokensCollection.find(Filters.eq("code", code)).first()
        document?.let {
            Json.decodeFromString(VerificationToken.serializer(), it.toJson(jsonSettings))
        }
    }

    override suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        val result = tokensCollection.deleteOne(Filters.eq("_id", id))
        result.deletedCount > 0
    }
}
