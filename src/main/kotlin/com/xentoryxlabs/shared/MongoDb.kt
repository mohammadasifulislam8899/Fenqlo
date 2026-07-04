package com.xentoryxlabs.shared

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import io.ktor.server.application.*
import io.ktor.server.config.*

fun Application.configureMongo(): MongoDatabase {
    val host = environment.config.tryGetString("db.mongo.host") ?: "127.0.0.1"
    val port = environment.config.tryGetString("db.mongo.port") ?: "27017"
    val databaseName = environment.config.tryGetString("db.mongo.database.name") ?: "myDatabase"

    val uri = "mongodb://$host:$port"
    val mongoClient = MongoClients.create(uri)
    val database = mongoClient.getDatabase(databaseName)

    // Close connection pool gracefully on application stop
    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return database
}
