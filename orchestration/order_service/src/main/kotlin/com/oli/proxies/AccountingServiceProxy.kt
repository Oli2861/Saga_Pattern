package com.oli.proxies

import com.oli.event.AuthorizationCommandEvent
import com.oli.event.MessageBroker
import com.oli.event.RabbitMQBroker
import com.oli.event.evaluateReply
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface AccountingServiceProxy{
    suspend fun authorize(sagaId: Int, userId: Int, paymentInfo: String): Boolean
}
class AccountingServiceProxyImpl(
    private val messageBroker: MessageBroker,
    private val logger: Logger
): AccountingServiceProxy{

    private val accountingServiceRequestChannel =
        System.getenv("ACCOUNTING_SERVICE_REQUEST_CHANNEL") ?: "accounting_service_request_channel"
    private val createOrderSagaReplyChannel =
        System.getenv("CREATE_ORDER_SAGA_REPLY_CHANNEL") ?: "create_order_saga_reply_channel"
    override suspend fun authorize(sagaId: Int, userId: Int, paymentInfo: String): Boolean {
        val event = AuthorizationCommandEvent(sagaId, userId, paymentInfo)
        logger.debug(event.toString())
        val reply = messageBroker.remoteProcedureCall(accountingServiceRequestChannel, createOrderSagaReplyChannel, event) ?: return false
        logger.debug(reply)
        return evaluateReply(reply, logger)
    }
}

fun main() = runBlocking{
    val proxy = AccountingServiceProxyImpl(RabbitMQBroker, LoggerFactory.getLogger(AccountingServiceProxy::class.java))
    val result = proxy.authorize(1, 1, "test")
    println(result)
}