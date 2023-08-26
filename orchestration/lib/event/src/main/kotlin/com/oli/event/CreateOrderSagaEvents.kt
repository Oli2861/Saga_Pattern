package com.oli.event

import kotlinx.serialization.Serializable
import com.oli.types.Address
import com.oli.types.OrderItem

interface CreateOrderSagaEvent: SagaEvent

@Serializable
data class ReplyEvent(
    override val sagaId: Int,
    val result: Boolean
): CreateOrderSagaEvent

@Serializable
data class VerifyCustomerCommandEvent(
    override val sagaId: Int,
    val customerId: Int,
    val address: Address
) : CreateOrderSagaEvent

@Serializable
data class CreateTicketCommandEvent(
    override val sagaId: Int,
    val customerId: Int,
    val orderItems: List<OrderItem>
) : CreateOrderSagaEvent

@Serializable
data class AuthorizationCommandEvent(
    override val sagaId: Int,
    val userId: Int,
    val paymentInfo: String
) : CreateOrderSagaEvent

@Serializable
data class ApproveTicketCommandEvent(
    override val sagaId: Int
) : CreateOrderSagaEvent

@Serializable
data class RejectTicketCommandEvent(
    override val sagaId: Int
) : CreateOrderSagaEvent
