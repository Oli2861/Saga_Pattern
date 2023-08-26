package com.oli.order

interface OrderSagaAssociationDAO {
    /**
     * Create an association between an Order entity and an CreateOrderSagaState entity.
     * @param orderID The associated OrderEntity.
     * @param sagaStateId The associated OrderSagaStateEntity.
     * @return The created entry.
     */
    suspend fun create(orderID: Int, sagaStateId: Int): OrderSagaAssociation?

    /**
     * Read an association for based on its saga id. Returns only one entry as this is supposed to be a 1:1 relation.
     * @param sagaId ID of the saga.
     * @return The read association or null if it does not exist.
     */
    suspend fun readUsingSagaId(sagaId: Int): OrderSagaAssociation?

    /**
     * Delete all associations for a given saga id.
     * @param sagaId ID of the saga.
     * @return The amount of affected rows.
     */
    suspend fun deleteAllForSaga(sagaId: Int): Int

    /**
     * Delete all associations for a given order id.
     * @param orderId ID of the order.
     * @return The amount of affected row.s
     */
    suspend fun deleteAllForOrder(orderId: Int): Int
}