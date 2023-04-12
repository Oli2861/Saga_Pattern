package com.oli.event

import com.oli.ticket.OrderItem
import kotlinx.serialization.Serializable

interface CreateOrderSagaEvent : SagaEvent

@Serializable
data class ReplyEvent(
    override val sagaId: Int,
    val result: Boolean?
): CreateOrderSagaEvent

@Serializable
data class CreateTicketCommandEvent(
    override val sagaId: Int,
    val customerId: Int,
    val orderItems: List<OrderItem>
) : CreateOrderSagaEvent

@Serializable
data class ApproveTicketCommandEvent(
    override val sagaId: Int
) : CreateOrderSagaEvent

@Serializable
data class RejectTicketCommandEvent(
    override val sagaId: Int
) : CreateOrderSagaEvent
