package com.oli.event

import com.oli.KitchenService
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import org.apache.commons.text.StringEscapeUtils
import org.koin.java.KoinJavaComponent.inject
import org.slf4j.Logger
import kotlin.IllegalArgumentException

object MessageReceiver {
    private val customerService by inject<KitchenService>(KitchenService::class.java)
    private val kitchenServiceRequestChannelName =
        System.getenv("KITCHEN_SERVICE_REQUEST_CHANNEL") ?: "kitchen_service_request_channel"

    fun init(scope: CoroutineScope) {
        RabbitMQBroker.listenForRPC(
            scope = scope,
            queueName = kitchenServiceRequestChannelName,
            onReceive = { correlationId: String, event: Event ->
                customerService.handleEvent(correlationId, event)
            },
            onCancel = {

            }
        )
    }

}