package com.xentoryxlabs.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class VerificationType {
    EMAIL_VERIFICATION,
    PASSWORD_RESET
}

@Serializable
data class VerificationToken(
    @SerialName("_id")
    val id: String,
    val userId: String,
    val code: String,
    val type: VerificationType,
    val expiresAt: Long,
    val createdAt: Long = System.currentTimeMillis()
)
