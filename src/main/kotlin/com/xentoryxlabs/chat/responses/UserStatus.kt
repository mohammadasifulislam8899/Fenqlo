package com.xentoryxlabs.chat.responses

import kotlinx.serialization.Serializable

@Serializable
data class UserStatus(
    val userId: String,
    val isOnline: Boolean,
    val lastSeen: Long? // null if online, epoch milliseconds timestamp if offline
)
