package com.oli.order

import com.oli.saga.CreateOrderSagaStates
import org.jetbrains.exposed.dao.id.IntIdTable

data class OrderSagaAssociation(
    val orderId: Int,
    val sagaId: Int
)

object OrderSagaAssociations: IntIdTable(){
    val orderId = reference("orderId", Orders)
    val sagaId = reference("sagaId", CreateOrderSagaStates)
}