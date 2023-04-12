package com.oli.order


class OrderRepositoryImpl(
    private val orderDAO: OrderDAO,
    private val orderSagaAssociationDAO: OrderSagaAssociationDAO
) : OrderRepository {

    override suspend fun createOrder(sagaId: Int, order: Order): Pair<Order?, OrderSagaAssociation?> {
        val createdOrder = orderDAO.createOrder(order) ?: return Pair(null, null)
        val createdAssociation = orderSagaAssociationDAO.create(createdOrder.id, sagaId)
        return Pair(createdOrder, createdAssociation)
    }

    override suspend fun createOrder(order: Order): Order? {
        return orderDAO.createOrder(order)
    }

    override suspend fun readOrder(orderId: Int): Order? {
        return orderDAO.readOrder(orderId)
    }

    override suspend fun readOrderForSagaId(sagaId: Int): Order? {
        val orderId = orderSagaAssociationDAO.readUsingSagaId(sagaId)?.orderId ?: return null
        return orderDAO.readOrder(orderId)
    }

    override suspend fun deleteOrder(orderId: Int): Int {
        orderSagaAssociationDAO.deleteAllForOrder(orderId)
        return orderDAO.deleteOrder(orderId)
    }

    override suspend fun updateOrderState(sagaId: Int, orderState: Int): Int {
        val orderSagaAssociation = orderSagaAssociationDAO.readUsingSagaId(sagaId) ?: return -1
        return orderDAO.updateOrderState(orderSagaAssociation.orderId, orderState)
    }

}