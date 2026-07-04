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
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.websocket.*
import kotlin.time.Duration.Companion.seconds
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.xentoryxlabs.chat.services.ChatService
import com.xentoryxlabs.chat.routes.chatRoutes

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

    // Install WebSockets plugin
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    // Install Authentication and configure JWT Provider
    install(Authentication) {
        jwt("auth-jwt") {
            val jwtSecret = "secret"
            val jwtIssuer = "https://jwt-provider-domain/"
            val jwtAudience = "jwt-audience"

            realm = "Access to chat server"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    install(Koin) {
        slf4jLogger()
        modules(module {
            single { mongoDb }
            single { redisDb }
            single<UserRepository> { MongoUserRepository(get()) }
            single<VerificationTokenRepository> { MongoVerificationTokenRepository(get()) }
            single { AuthService(get(), get()) }
            single<com.xentoryxlabs.chat.repositories.ChatRepository> { com.xentoryxlabs.chat.repositories.MongoChatRepository(get()) }
            single { ChatService(get(), get()) }
        })
    }

    // Configure HTTP API Routing
    routing {
        authRoutes()
        chatRoutes()
    }
}
