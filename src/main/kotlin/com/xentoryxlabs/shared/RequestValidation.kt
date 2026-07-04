package com.xentoryxlabs.shared

import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

import com.xentoryxlabs.auth.requests.SignupRequest
import com.xentoryxlabs.auth.requests.LoginRequest
import com.xentoryxlabs.auth.requests.VerifyOtpRequest

fun Application.configureRequestValidation() {
    install(RequestValidation) {
        validate<SignupRequest> { signup ->
            val reasons = mutableListOf<String>()
            if (signup.username.isBlank()) {
                reasons.add("Username cannot be blank")
            }
            if (signup.email.isBlank() || !signup.email.contains("@") || !signup.email.contains(".")) {
                reasons.add("Invalid email format")
            }
            if (signup.password.length < 6) {
                reasons.add("Password must be at least 6 characters long")
            }
            if (reasons.isNotEmpty()) {
                ValidationResult.Invalid(reasons)
            } else {
                ValidationResult.Valid
            }
        }

        validate<LoginRequest> { login ->
            val reasons = mutableListOf<String>()
            if (login.username.isBlank()) {
                reasons.add("Username cannot be blank")
            }
            if (login.password.isBlank()) {
                reasons.add("Password cannot be blank")
            }
            if (reasons.isNotEmpty()) {
                ValidationResult.Invalid(reasons)
            } else {
                ValidationResult.Valid
            }
        }

        validate<VerifyOtpRequest> { verify ->
            val reasons = mutableListOf<String>()
            if (verify.code.isBlank()) {
                reasons.add("Verification code cannot be blank")
            } else if (verify.code.length != 6 || verify.code.any { !it.isDigit() }) {
                reasons.add("Verification code must be exactly 6 digits")
            }
            if (reasons.isNotEmpty()) {
                ValidationResult.Invalid(reasons)
            } else {
                ValidationResult.Valid
            }
        }
    }
}
