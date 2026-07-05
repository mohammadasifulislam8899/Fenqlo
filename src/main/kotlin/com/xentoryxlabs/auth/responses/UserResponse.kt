package com.xentoryxlabs.auth.responses

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val isVerified: Boolean
)
