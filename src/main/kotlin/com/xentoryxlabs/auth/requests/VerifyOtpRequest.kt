package com.xentoryxlabs.auth.requests

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequest(
    val code: String
)
