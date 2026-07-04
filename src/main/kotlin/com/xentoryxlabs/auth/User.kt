package com.xentoryxlabs.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("_id")
    val id: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
