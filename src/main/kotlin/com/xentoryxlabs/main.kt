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
import com.xentoryxlabs.auth.UserRepository
import com.xentoryxlabs.auth.MongoUserRepository
import com.xentoryxlabs.auth.VerificationTokenRepository
import com.xentoryxlabs.auth.MongoVerificationTokenRepository
import com.xentoryxlabs.auth.AuthService

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
}
