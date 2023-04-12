package com.oli.plugins

import com.oli.address.Address
import com.oli.customer.Customer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class SerializationTests {
    @Test
    fun testCustomerSerialization() {
        val expected =
            """{"age":22,"firstName":"Max","lastName":"Mustermann","addresses":[{"postCode":8123,"city":"Testing Town","houseNumber":"93a"}]}"""
        val actual = Json.encodeToString(
            Customer(
                age = 22,
                firstName = "Max",
                lastName = "Mustermann",
                addresses = listOf(Address(postCode = 8123, city = "Testing Town", houseNumber = "93a"))
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testCustomerDeserialization() {
        val string =
            """{"age":22,"firstName":"Max","lastName":"Mustermann","addresses":[{"postCode":8123,"city":"Testing Town","houseNumber":"93a"}]}"""
        val actual = Json.decodeFromString<Customer>(string)
        val expected = Customer(
            age = 22,
            firstName = "Max",
            lastName = "Mustermann",
            addresses = listOf(Address(postCode = 8123, city = "Testing Town", houseNumber = "93a"))
        )
        assertEquals(expected, actual)
    }
}