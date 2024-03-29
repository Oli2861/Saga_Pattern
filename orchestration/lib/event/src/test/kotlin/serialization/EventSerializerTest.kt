package serialization

import com.oli.event.*
import com.oli.serialization.EventSerializer
import com.oli.types.OrderItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventSerializerTest {

    @Test
    fun serializeAuthorizationCommandEventTest(){
        val event: Event = AuthorizationCommandEvent(2, 3, "test")
        val actual = EventSerializer.serialize(event)

        val expected = "{\"type\":\"com.oli.event.AuthorizationCommandEvent\",\"sagaId\":2,\"userId\":3,\"paymentInfo\":\"test\"}"
        assertEquals(expected, actual)
    }

    @Test
    fun deserializeAuthorizationCommandEventTest() {
        val actual = EventSerializer.deserialize("{\"type\":\"com.oli.event.AuthorizationCommandEvent\",\"sagaId\":2,\"userId\":3,\"paymentInfo\":\"test\"}")

        val expected: Event = AuthorizationCommandEvent(2, 3, "test")
        assertTrue(actual is SagaEvent)
        assertTrue(actual is CreateOrderSagaEvent)
        assertTrue(actual is AuthorizationCommandEvent)
        assertEquals(expected, actual)
    }

    @Test
    fun serializeAndDeserializeAuthorizationCommandEvent(){
        val event: Event = AuthorizationCommandEvent(1, 2, "test")
        val serialized = EventSerializer.serialize(event)
        val deserialized = EventSerializer.deserialize(serialized)

        assertEquals(event, deserialized)
    }

    @Test
    fun serializeAuthorizationReplyEventTest(){
        val event: Event = ReplyEvent(1, false)
        val actual = EventSerializer.serialize(event)

        val expected = "{\"type\":\"com.oli.event.ReplyEvent\",\"sagaId\":1,\"result\":false}"
        assertEquals(expected, actual)
    }

    @Test
    fun deserializeAuthorizationReplyEventTest() {
        val actual = EventSerializer.deserialize("{\"type\":\"com.oli.event.ReplyEvent\",\"sagaId\":1,\"result\":false}")

        val expected: Event = ReplyEvent(1, false)
        assertTrue(actual is ReplyEvent)
        assertEquals(expected, actual)
    }

    @Test
    fun serializeAndDeserializeReplyCommandEvent(){
        val event: Event = ReplyEvent(1, false)
        val serialized = EventSerializer.serialize(event)
        val deserialized = EventSerializer.deserialize(serialized)

        assertTrue(deserialized is SagaEvent)
        assertTrue(deserialized is CreateOrderSagaEvent)
        assertTrue(deserialized is ReplyEvent)
        assertEquals(event, deserialized)
    }

    @Test
    fun testDeserializeCreateTicketCommandEvent(){
        val msg = "{\"type\":\"com.oli.event.CreateTicketCommandEvent\",\"sagaId\":1,\"customerId\":1,\"orderItems\":[{\"articleNumber\":1,\"amount\":2},{\"articleNumber\":3,\"amount\":4}]}"
        val actual = EventSerializer.deserialize(msg)
        val expected = CreateTicketCommandEvent(1, 1, listOf(OrderItem(1, 2 ), OrderItem(3, 4)))
        assertEquals(expected, actual)
    }
}