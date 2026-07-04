package com.xentoryxlabs

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.*

class ServerTest {

    @Test
    fun `test root endpoint`() = testApplication {
        // verify server root returns 404 since it has no routes
        assertEquals(HttpStatusCode.NotFound, client.get("/").status)
    }

}
