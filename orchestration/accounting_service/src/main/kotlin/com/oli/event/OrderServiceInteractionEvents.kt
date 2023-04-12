package com.oli.event

import kotlinx.serialization.Serializable

interface CreateOrderSagaEvent: SagaEvent

@Serializable
data class AuthorizationCommandEvent(
    override val sagaId: Int,
    val userId: Int,
    val paymentInfo: String
) : CreateOrderSagaEvent

@Serializable
data class ReplyEvent(
    override val sagaId: Int,
    val result: Boolean
): CreateOrderSagaEvent
