package com.oli.broker

import com.oli.event.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

interface MessageBroker {
    suspend fun remoteProcedureCall(queueName: String, replyQueueName: String, event: Event): String?
    fun listenForRPC(scope: CoroutineScope, queueName: String, onReceive: suspend (String, Event) -> Event, onCancel: () -> Unit)

    suspend fun listenToRequestChannel(queueName: String): SharedFlow<Event>
    suspend fun publishToQueue(queueName: String, event: Event)

    suspend fun closeConnection()
}
