package com.oli.plugins

import com.oli.order.Order
import com.oli.order.OrderItem
import com.oli.proxies.Address
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
        val order = Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1)))
        val actual = Json.encodeToString(order)
        val expected = "{\"id\":0,\"userId\":0,\"timestamp\":1676973532903,\"orderState\":1,\"items\":[{\"articleNumber\":1,\"amount\":1},{\"articleNumber\":2,\"amount\":2}]}"
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
        val jsonString = "{\"id\":0,\"userId\":0,\"timestamp\":1676973532903,\"orderState\":1,\"items\":[{\"articleNumber\":1,\"amount\":10},{\"articleNumber\":2,\"amount\":3}]}"
        val actual = Json.decodeFromString<Order>(jsonString)
        val expected = Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1)))
        assertEquals(expected, actual)
    }

}