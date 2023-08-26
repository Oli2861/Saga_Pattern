package com.oli.event

import com.oli.broker.MessageBroker
import com.oli.order.OrderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

enum class ReplyChannels(val channelName: String) {
    CREATE_ORDER_SAGA(System.getenv("CREATE_ORDER_SAGA_REPLY_CHANNEL") ?: "create_order_saga_reply_channel")
}

object MessageReceiver {
    private val orderService by inject<OrderService>(OrderService::class.java)
    private val messageBroker by inject<MessageBroker>(MessageBroker::class.java)

    fun init(scope: CoroutineScope, listenedReplyChannels: List<ReplyChannels>) {
        listenedReplyChannels.forEach { channel ->
            scope.launch {
                messageBroker.listenToRequestChannel(channel.channelName).collect {
                    orderService.handleCreateOrderSagaReply(it)
                }
            }
        }
    }
}