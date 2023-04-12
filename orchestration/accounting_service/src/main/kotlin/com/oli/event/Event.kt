package com.oli.event

import kotlinx.serialization.Serializable

interface Event {
}

@Serializable
data class ErrorEvent(
    val message: String
) : Event

interface SagaEvent : Event {
    val sagaId: Int
}
