package com.oli

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.http.*
import com.oli.plugins.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            module(true, false)
        }
    }
}
