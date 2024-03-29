package com.oli.event

import com.oli.broker.MessageBroker
import com.oli.accounting.AccountingService
import kotlinx.coroutines.CoroutineScope
import org.koin.java.KoinJavaComponent.inject

object MessageReceiver {
    private val accountingService by inject<AccountingService>(AccountingService::class.java)
    private val messageBroker by inject<MessageBroker>(MessageBroker::class.java)
    private val customerServiceRequestChannelName =
        System.getenv("ACCOUNTING_SERVICE_REQUEST_CHANNEL") ?: "accounting_service_request_channel"

    fun init(scope: CoroutineScope) {
        messageBroker.listenForRPC(
            scope = scope,
            queueName = customerServiceRequestChannelName,
            onReceive = { correlationId: String, event: Event ->
                accountingService.handleEvent(correlationId, event)
            },
            onCancel = {
                // TODO
            }
        )
    }

}