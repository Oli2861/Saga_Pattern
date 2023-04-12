package com.oli.saga

interface CreateOrderSagaStateDAO {
    suspend fun create(createOrderSagaState: CreateOrderSagaState): CreateOrderSagaState?

    suspend fun read(id: Int): CreateOrderSagaState?

    suspend fun delete(id: Int): Int
    suspend fun update(sagaState: SagaState): Int
}