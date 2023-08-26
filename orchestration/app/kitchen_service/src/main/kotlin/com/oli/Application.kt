package com.oli

import com.oli.event.MessageReceiver
import com.oli.persistence.DatabaseFactory
import com.oli.plugins.configureMonitoring
import com.oli.plugins.configureRouting
import com.oli.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun main() {
    embeddedServer(Netty, port = 8083, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(useEmbeddedDatabase: Boolean = false) {
    DatabaseFactory.init(useEmbeddedDatabase)
    configureKoin()

    configureMonitoring()
    configureSerialization()
    configureRouting()

    val coroutineScope = CoroutineScope(this.coroutineContext + Dispatchers.IO)
    MessageReceiver.init(coroutineScope)
}
