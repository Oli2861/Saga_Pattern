package com.oli

import com.oli.order.orderModule
import com.oli.persistence.DatabaseFactory
import com.oli.plugins.configureHTTP
import com.oli.plugins.configureRouting
import com.oli.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(useEmbeddedDatabase: Boolean = false) {
    configureKoin()
    DatabaseFactory.init(useEmbeddedDatabase)

    configureRouting()
    configureHTTP()
    configureSerialization()

    orderModule()
}
