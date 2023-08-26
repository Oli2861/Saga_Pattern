package com.oli.plugins

import com.oli.order.Order
import com.oli.types.Address
import com.oli.types.OrderItem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import java.sql.Timestamp
import kotlin.test.assertEquals
class SerializationTests {
    // Test serialization
    @Test
    fun testSerialize() {
        val time = 1676973532903L
        val expected = JsonPrimitive(time)
        val actual = Json.encodeToJsonElement(TimestampAsLongSerializer, Timestamp(time))
        assertEquals(expected, actual)
    }

    @Test
    fun testOrderSerialization() {
        val time = 1676973532903L
        val order = Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), timestamp = Timestamp(time), paymentInfo = "test", items= listOf(
            OrderItem(1, 1)
        ))
        val actual = Json.encodeToString(order)
        val expected = "{\"customerId\":1,\"address\":{\"postCode\":12345,\"city\":\"Mustertown\",\"houseNumber\":\"5e\"},\"paymentInfo\":\"test\",\"timestamp\":${time},\"items\":[{\"articleNumber\":1,\"amount\":1}]}"
        assertEquals(expected, actual)
    }

    // Test deserialization
    @Test
    fun testDeserialize() {
        val time = 1676973532903L
        val jsonElement = Json.encodeToJsonElement(TimestampAsLongSerializer, Timestamp(time))
        val actual = Json.decodeFromJsonElement(TimestampAsLongSerializer, jsonElement)
        val expected = Timestamp(time)
        assertEquals(expected, actual)
    }

    @Test
    fun testOrderDeserialization() {
        val time = 1676973532903L
        val jsonString = "{\"customerId\":1,\"address\":{\"postCode\":12345,\"city\":\"Mustertown\",\"houseNumber\":\"5e\"},\"paymentInfo\":\"test\",\"timestamp\":${time},\"items\":[{\"articleNumber\":1,\"amount\":1}]}"
        val actual = Json.decodeFromString<Order>(jsonString)
        val expected = Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), timestamp = Timestamp(time), paymentInfo = "test", items= listOf(OrderItem(1, 1)))
        assertEquals(expected, actual)
    }

}