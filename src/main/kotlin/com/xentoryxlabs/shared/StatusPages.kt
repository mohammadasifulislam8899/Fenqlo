package com.xentoryxlabs.shared

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Resource not found")
            )
        }
        exception<RequestValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("errors" to cause.reasons)
            )
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception: ", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.localizedMessage ?: "Internal Server Error"))
            )
        }
    }
}
