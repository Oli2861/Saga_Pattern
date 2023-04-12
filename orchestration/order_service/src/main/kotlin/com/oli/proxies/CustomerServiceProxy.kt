package com.oli.proxies

import com.oli.event.*
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface CustomerServiceProxy {
    suspend fun sendVerifyCustomerDetailsCommand(sagaId: Int, customerId: Int, address: Address): Boolean
}

class CustomerServiceProxyImpl(
    private val messageBroker: MessageBroker,
    private val logger: Logger
) : CustomerServiceProxy {
    private val customerServiceRequestChannel = System.getenv("CUSTOMER_SERVICE_REQUEST_CHANNEL") ?: "customer_service_request_channel"
    private val createOrderSagaReplyChannel = System.getenv("CREATE_ORDER_SAGA_REPLY_CHANNEL") ?: "create_order_saga_reply_channel"

    override suspend fun sendVerifyCustomerDetailsCommand(sagaId: Int, customerId: Int, address: Address): Boolean {
        val command = VerifyCustomerCommandEvent(sagaId, customerId, address)
        val reply = messageBroker.remoteProcedureCall(customerServiceRequestChannel, createOrderSagaReplyChannel, command) ?: return false
        return evaluateReply(reply, logger)
    }
}

fun main() = runBlocking {
    println(CustomerServiceProxyImpl(RabbitMQBroker, LoggerFactory.getLogger(CustomerServiceProxyImpl::class.java)).sendVerifyCustomerDetailsCommand(1, 1, Address(9123, "Mustertown", "2c")))
}
