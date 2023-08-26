package com.oli.proxies

import com.oli.order.Order
import com.oli.order.OrderSagaAssociation

interface OrderServiceProxy {
    suspend fun createOrder(sagaId: Int, order: Order): Pair<Order?, OrderSagaAssociation?>
    suspend fun updateOrderState(sagaId: Int, orderState: Int): Boolean
}