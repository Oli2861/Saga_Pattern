package com.oli.order

interface OrderDAO {
    suspend fun createOrder(order: Order): Order?
    suspend fun readOrder(id: Int): Order?
    suspend fun deleteOrder(id: Int): Int
    suspend fun updateOrderState(id: Int, newState: Int): Int
}