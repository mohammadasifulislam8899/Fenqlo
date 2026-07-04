package com.xentoryxlabs.auth

interface UserRepository {
    suspend fun findById(id: String): User?
    suspend fun findByUsername(username: String): User?
    suspend fun findByEmail(email: String): User?
    suspend fun create(user: User): Boolean
    suspend fun verifyUser(id: String): Boolean
}
