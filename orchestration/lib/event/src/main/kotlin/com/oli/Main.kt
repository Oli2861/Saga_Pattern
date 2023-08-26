package com.oli

import com.oli.broker.RabbitMQConnectionManager
import com.oli.event.SimpleEvent
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val rabbit = RabbitMQConnectionManager.getInstance("localhost")
    launch { listen(rabbit) }
    emit(rabbit)
    rabbit.closeConnection()
    println("Done")
}

suspend fun emit(rabbit: RabbitMQConnectionManager) {
    rabbit.publishToQueue("test", SimpleEvent("test"))
}

suspend fun listen(rabbit: RabbitMQConnectionManager) = coroutineScope {
    rabbit.listenToRequestChannel("test").collectLatest {
        println(it)
        rabbit.closeConnection()
        this.cancel()
    }
}
