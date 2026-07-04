package com.xentoryxlabs.auth

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class SignupRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class VerifyOtpRequest(
    val code: String
)

@Serializable
data class AuthResponse(
    val token: String
)

fun Route.authRoutes() {
    val authService by inject<AuthService>()

    route("/api/auth") {
        post("/signup") {
            val request = call.receive<SignupRequest>()
            val result = authService.signup(request.username, request.email, request.password)
            if (result != null) {
                call.respond(HttpStatusCode.Created, mapOf("message" to "Verification code generated. Check server console logs."))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username or Email already taken"))
            }
        }

        post("/verify") {
            val request = call.receive<VerifyOtpRequest>()
            val success = authService.verifyOtp(request.code)
            if (success) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Account verified successfully. You can now log in."))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid or expired verification code"))
            }
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val token = authService.login(request.username, request.password)
            if (token != null) {
                call.respond(HttpStatusCode.OK, AuthResponse(token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials or account not verified"))
            }
        }
    }
}
