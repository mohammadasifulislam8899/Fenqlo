package com.xentoryxlabs.shared

import io.ktor.server.application.*
import io.ktor.server.config.*
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

fun Application.configureRedis(): JedisPool {
    val host = environment.config.tryGetString("redis.host") ?: "127.0.0.1"
    val port = environment.config.tryGetString("redis.port")?.toInt() ?: 6379

    val poolConfig = JedisPoolConfig().apply {
        maxTotal = 20
        maxIdle = 10
        minIdle = 2
    }

    val jedisPool = JedisPool(poolConfig, host, port)

    // Close connection pool gracefully on application stop
    monitor.subscribe(ApplicationStopped) {
        jedisPool.close()
    }

    return jedisPool
}
