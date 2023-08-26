package com.oli.order

interface OrderRepository {
    /**
     * Create an order that is associated with a saga.
     * @param sagaId ID of the associated saga.
     * @param order Values for the order to be created.
     * @return The stored order object or null if there is no saga for the given ID.
     */
    suspend fun createOrder(sagaId: Int, order: Order): Pair<Order?, OrderSagaAssociation?>

    suspend fun createOrder(order: Order): Order?

    /**
     * Read an order from the database.
     * @param orderId ID of the order.
     * @return Order or null if there is no order for the given ID.
     */
    suspend fun readOrder(orderId: Int): Order?

    suspend fun readOrderForSagaId(sagaId: Int): Order?

    /**
     * Delete an order.
     * @param orderId ID of the order to be deleted.
     * @return Amount of deleted rows.
     */
    suspend fun deleteOrder(orderId: Int): Int

    /**
     * Update the state of all orders associated with a saga.
     * @param sagaId ID of the associated saga.
     * @param orderState New state for the order entities.
     * @return The amount of affected rows.
     */
    suspend fun updateOrderState(sagaId: Int, orderState: Int): Int

}
