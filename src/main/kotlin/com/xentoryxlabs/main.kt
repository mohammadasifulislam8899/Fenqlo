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
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

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
        })
    }

    // Configure HTTP API Routing
    routing {
        authRoutes()
    }
}
