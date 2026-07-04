package com.xentoryxlabs.shared

import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureRequestValidation() {
    install(RequestValidation) {
        // Validation rules can be added here
    }
}
