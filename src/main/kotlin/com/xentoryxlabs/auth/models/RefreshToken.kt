package com.xentoryxlabs.auth.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshToken(
    @SerialName("_id")
    val id: String,
    val userId: String,
    val token: String,
    val expiresAt: Long,
    val isRevoked: Boolean = false
)
