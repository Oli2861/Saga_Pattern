package com.oli.event

import com.oli.customer.CustomerService
import kotlinx.coroutines.CoroutineScope
import org.koin.java.KoinJavaComponent.inject

object MessageReceiver {
    private val customerService by inject<CustomerService>(CustomerService::class.java)
    private val customerServiceRequestChannelName =
        System.getenv("CUSTOMER_SERVICE_REQUEST_CHANNEL") ?: "customer_service_request_channel"

    fun init(scope: CoroutineScope) {
        RabbitMQBroker.listenForRPC(
            scope = scope,
            queueName = customerServiceRequestChannelName,
            onReceive = { correlationId: String, event: Event ->
                customerService.handleEvent(correlationId, event)
            },
            onCancel = {

            }
        )
    }

}