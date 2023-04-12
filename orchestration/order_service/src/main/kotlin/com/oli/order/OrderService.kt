package com.oli.order

import com.oli.proxies.OrderServiceProxy
import com.oli.saga.CreateOrderSagaManager
import com.oli.utility.stringIdToInt
import org.slf4j.Logger

class OrderService(
    private val orderRepository: OrderRepository,
    private val createOrderSagaManager: CreateOrderSagaManager,
    private val logger: Logger
): OrderServiceProxy {

    override suspend fun createOrder(sagaId: Int, order: Order): Pair<Order?, OrderSagaAssociation?> {
        return orderRepository.createOrder(sagaId, order)
    }

    suspend fun readOrder(id: String): Order? {
        val orderId = stringIdToInt(id, logger) ?: return null
        return orderRepository.readOrder(orderId)
    }

    suspend fun deleteOrder(id: String): Int {
        val orderId = stringIdToInt(id, logger) ?: return -1
        return orderRepository.deleteOrder(orderId)
    }

    override suspend fun updateOrderState(sagaId: Int, orderState: Int): Boolean {
        return orderRepository.updateOrderState(sagaId, orderState) >= 1
    }

    suspend fun createOrderSaga(order: Order): Pair<Int, Boolean>? {
        val (success, sagaId) = createOrderSagaManager.createAndExecuteSaga(order) ?: return null
        val created = orderRepository.readOrderForSagaId(sagaId) ?: return null
        return Pair(created.id, success)
    }

}