package com.xentoryxlabs.auth



interface VerificationTokenRepository {
    suspend fun create(token: VerificationToken): Boolean
    suspend fun findByCode(code: String): VerificationToken?
    suspend fun delete(id: String): Boolean
}
