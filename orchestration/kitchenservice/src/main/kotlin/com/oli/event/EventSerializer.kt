package com.oli.event

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.jvm.Throws


object EventSerializer {

    private val format = Json {
        serializersModule = SerializersModule {
            polymorphic(Event::class) {
                // CreateOrderSagaEvents
                subclass(CreateTicketCommandEvent::class, CreateTicketCommandEvent.serializer())
                subclass(ApproveTicketCommandEvent::class, ApproveTicketCommandEvent.serializer())
                subclass(RejectTicketCommandEvent::class, RejectTicketCommandEvent.serializer())
                subclass(ErrorEvent::class, ErrorEvent.serializer())
                subclass(ReplyEvent::class, ReplyEvent.serializer())
            }
        }
    }

    /**
     * Deserialize a event.
     * @param event The event serialized as a string.
     * @return Deserialized event.
     * @throws IllegalArgumentException If the event is not resolvable to a subclass of event.
     */
    @Throws(IllegalArgumentException::class)
    fun deserialize(event: String): Event = format.decodeFromString(event)
    fun serialize(event: Event): String = format.encodeToString(event)

}