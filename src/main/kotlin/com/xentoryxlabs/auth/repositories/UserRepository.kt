package com.xentoryxlabs.auth.repositories

import com.xentoryxlabs.auth.models.User

interface UserRepository {
    suspend fun findById(id: String): User?
    suspend fun findByUsername(username: String): User?
    suspend fun findByEmail(email: String): User?
    suspend fun create(user: User): Boolean
    suspend fun verifyUser(id: String): Boolean
    suspend fun findAll(): List<User>
}
