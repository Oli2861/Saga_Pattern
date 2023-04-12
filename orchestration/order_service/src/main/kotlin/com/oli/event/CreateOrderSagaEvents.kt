package com.oli.event

import com.oli.order.OrderItem
import com.oli.proxies.Address
import kotlinx.serialization.Serializable

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
