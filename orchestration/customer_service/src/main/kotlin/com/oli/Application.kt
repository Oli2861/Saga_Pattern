package com.oli

import com.oli.address.configureAddressRouting
import com.oli.customer.customerModule
import com.oli.event.MessageReceiver
import com.oli.persistence.DatabaseFactory
import com.oli.plugins.configureMonitoring
import com.oli.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun main() {
    embeddedServer(Netty, port = 8082, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

/**
 * Base module of the application. Configures all submodules.
 * @param useEmbedded Whether the application is being tested. This is relevant, as the DatabaseFactory will use an embedded H2 in memory database instead of a postgres.
 * @param listenToMessages Whether to listen to messages from the message broker.
 */
fun Application.module(useEmbedded: Boolean = false, listenToMessages: Boolean = true) {
    DatabaseFactory.init(useEmbedded)
    configureKoin()

    configureMonitoring()
    configureSerialization()

    customerModule()
    configureAddressRouting()

    if(listenToMessages)
    MessageReceiver.init(CoroutineScope(this.coroutineContext + Dispatchers.IO))
}
