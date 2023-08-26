package com.oli.event

import com.oli.address.Address
import kotlinx.serialization.Serializable

interface CreateOrderSagaEvent : SagaEvent

@Serializable
data class VerifyCustomerCommandEvent(
    override val sagaId: Int,
    val customerId: Int,
    val address: Address
) : CreateOrderSagaEvent

@Serializable
data class ReplyEvent(
    override val sagaId: Int,
    val result: Boolean
): CreateOrderSagaEvent
