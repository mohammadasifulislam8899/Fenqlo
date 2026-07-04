package com.xentoryxlabs

import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.dsl.module
import org.koin.logger.slf4jLogger
import com.xentoryxlabs.shared.configureMongo
import com.xentoryxlabs.shared.configureRedis
import com.xentoryxlabs.shared.configureCallLogging
import com.xentoryxlabs.shared.configureStatusPages
import com.xentoryxlabs.shared.configureRequestValidation
import com.xentoryxlabs.auth.repositories.UserRepository
import com.xentoryxlabs.auth.repositories.MongoUserRepository
import com.xentoryxlabs.auth.repositories.VerificationTokenRepository
import com.xentoryxlabs.auth.repositories.MongoVerificationTokenRepository
import com.xentoryxlabs.auth.services.AuthService
import com.xentoryxlabs.auth.routes.authRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val mongoDb = configureMongo()
    val redisDb = configureRedis()

    // Initialize HTTP and Validation plugins
    configureCallLogging()
    configureStatusPages()
    configureRequestValidation()

    // Install ContentNegotiation for JSON parsing/serialization
    install(ContentNegotiation) {
        json()
    }

    install(Koin) {
        slf4jLogger()
        modules(module {
            single { mongoDb }
            single { redisDb }
            single<UserRepository> { MongoUserRepository(get()) }
            single<VerificationTokenRepository> { MongoVerificationTokenRepository(get()) }
            single { AuthService(get(), get()) }
        })
    }

    // Configure HTTP API Routing
    routing {
        authRoutes()
    }
}
