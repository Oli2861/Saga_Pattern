package com.oli.proxies

import com.oli.event.*
import com.oli.order.OrderItem
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface KitchenServiceProxy {
    suspend fun createTicket(sagaId: Int, customerId: Int, menuItems: List<OrderItem>): Boolean
    suspend fun rejectTicket(sagaId: Int): Boolean
    suspend fun approveTicket(sagaId: Int): Boolean
}

class KitchenServiceProxyImpl(
    private val messageBroker: MessageBroker,
    private val logger: Logger
) : KitchenServiceProxy {

    private val customerServiceRequestChannel =
        System.getenv("KITCHEN_SERVICE_REQUEST_CHANNEL") ?: "kitchen_service_request_channel"
    private val createOrderSagaReplyChannel =
        System.getenv("CREATE_ORDER_SAGA_REPLY_CHANNEL") ?: "create_order_saga_reply_channel"

    override suspend fun createTicket(sagaId: Int, customerId: Int, menuItems: List<OrderItem>): Boolean {
        val event = CreateTicketCommandEvent(sagaId, customerId, menuItems)
        logger.debug(event.toString())
        val reply = messageBroker.remoteProcedureCall(customerServiceRequestChannel, createOrderSagaReplyChannel, event)
            ?: return false
        logger.debug(reply)
        return evaluateReply(reply, logger)
    }

    override suspend fun rejectTicket(sagaId: Int): Boolean {
        val event = RejectTicketCommandEvent(sagaId)
        val reply = messageBroker.remoteProcedureCall(customerServiceRequestChannel, createOrderSagaReplyChannel, event)
            ?: return false
        logger.debug(reply)
        return evaluateReply(reply, logger)
    }

    override suspend fun approveTicket(sagaId: Int): Boolean {
        val event = ApproveTicketCommandEvent(sagaId)
        val reply = messageBroker.remoteProcedureCall(customerServiceRequestChannel, createOrderSagaReplyChannel, event)
            ?: return false
        logger.debug(reply)
        return evaluateReply(reply, logger)
    }

}

fun main() = runBlocking {
    val proxy = KitchenServiceProxyImpl(RabbitMQBroker, LoggerFactory.getLogger(KitchenServiceProxyImpl::class.java))
    val reply = if (false) {
        proxy.createTicket(1, 1, listOf(OrderItem(1, 2), OrderItem(3, 4)))
    } else if (true) {
        proxy.rejectTicket(1)
    } else {
        proxy.approveTicket(1)
    }
    println(reply)
}
