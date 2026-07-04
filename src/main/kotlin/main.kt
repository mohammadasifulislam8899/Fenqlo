package com.xentoryxlabs

import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.dsl.module
import org.koin.logger.slf4jLogger
import com.xentoryxlabs.shared.configureMongo
import com.xentoryxlabs.shared.configureRedis

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val mongoDb = configureMongo()
    val redisDb = configureRedis()

    install(Koin) {
        slf4jLogger()
        modules(module {
            single { mongoDb }
            single { redisDb }
        })
    }
}

