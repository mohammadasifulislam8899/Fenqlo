package com.xentoryxlabs.auth.repositories

import com.xentoryxlabs.auth.models.User

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings

class MongoUserRepository(
    private val database: MongoDatabase
) : UserRepository {

    private val usersCollection = database.getCollection("users")

    // Configures MongoDB JSON writer to output standard clean JSON instead of extended BSON types
    private val jsonSettings = JsonWriterSettings.builder()
        .outputMode(JsonMode.RELAXED)
        .build()

    override suspend fun findById(id: String): User? = withContext(Dispatchers.IO) {
        val document = usersCollection.find(Filters.eq("id", id)).first()
        document?.let {
            Json.decodeFromString(User.serializer(), it.toJson(jsonSettings))
        }
    }

    override suspend fun findByUsername(username: String): User? = withContext(Dispatchers.IO) {
        val document = usersCollection.find(Filters.eq("username", username)).first()
        document?.let {
            Json.decodeFromString(User.serializer(), it.toJson(jsonSettings))
        }
    }

    override suspend fun findByEmail(email: String): User? = withContext(Dispatchers.IO) {
        val document = usersCollection.find(Filters.eq("email", email)).first()
        document?.let {
            Json.decodeFromString(User.serializer(), it.toJson(jsonSettings))
        }
    }

    override suspend fun create(user: User): Boolean = withContext(Dispatchers.IO) {
        val jsonString = Json.encodeToString(User.serializer(), user)
        val document = Document.parse(jsonString)
        val result = usersCollection.insertOne(document)
        result.wasAcknowledged()
    }

    override suspend fun verifyUser(id: String): Boolean = withContext(Dispatchers.IO) {
        val result = usersCollection.updateOne(
            Filters.eq("id", id),
            Updates.set("isVerified", true)
        )
        result.matchedCount > 0
    }

    override suspend fun findAll(): List<User> = withContext(Dispatchers.IO) {
        val users = mutableListOf<User>()
        usersCollection.find().forEach { doc ->
            val user = Json.decodeFromString(User.serializer(), doc.toJson(jsonSettings))
            users.add(user)
        }
        users
    }
}
