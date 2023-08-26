package com.oli

import com.oli.accounting.accountingModule
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
    embeddedServer(Netty, port = 8084, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(isTest: Boolean = false, listenToMessages: Boolean = false) {
    DatabaseFactory.init(isTest)
    configureKoin()

    configureSerialization()
    configureMonitoring()
    accountingModule()
    if(listenToMessages)
    MessageReceiver.init(CoroutineScope(this.coroutineContext + Dispatchers.IO))

}
