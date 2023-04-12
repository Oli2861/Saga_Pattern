package com.oli

import com.oli.address.Address
import com.oli.customer.Customer
import com.oli.customer.CustomerNoAddress
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ApplicationTests {
    @Test
    fun testCreateCustomer() = testApplication {
        application {
            // True to use h2 in-memory db instead of postgres
            // Don't listen to message broker messages in testing scenarios
            module(true, false)
        }

        // Create customer
        val customer = Json.encodeToString(
            Customer(
                age = 22, firstName = "Max", lastName = "Mustermann", addresses = listOf(
                    Address(postCode = 8123, city = "Testing Town", houseNumber = "93a")
                )
            )
        )
        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(customer)
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertIs<Int>(response.bodyAsText().toInt())
    }

    @Test
    fun testGetCustomer() = testApplication {
        application {
            module(true, false)
        }

        // Create customer
        val customer = Json.encodeToString(
            Customer(
                age = 22, firstName = "Max", lastName = "Mustermann", addresses = listOf(
                    Address(postCode = 8123, city = "Testing Town", houseNumber = "93a")
                )
            )
        )
        val created = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(customer)
        }
        val id = created.bodyAsText().toInt()

        // Read customer with id returned by the create request
        val readResponse = client.get("/users/${created.bodyAsText()}") {
            accept(ContentType.Application.Json)
        }

        val expected =
            Json.encodeToString(Customer(id, 22, "Max", "Mustermann", listOf(Address(id, 8123, "Testing Town", "93a"))))
        assertEquals(HttpStatusCode.OK, readResponse.status)
        assertEquals(expected, readResponse.bodyAsText())
    }

    @Test
    fun testGetDoesNotExist() = testApplication {
        application {
            module(true, false)
        }
        val readResponse = client.get("/users/-1") {
            accept(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.NotFound, readResponse.status)
    }

    @Test
    fun updateCustomer() = testApplication {
        application {
            module(true, false)
        }

        // Create customer
        val customer = Json.encodeToString(
            Customer(
                age = 22, firstName = "Max", lastName = "Mustermann", addresses = listOf(
                    Address(postCode = 8123, city = "Testing Town", houseNumber = "93a")
                )
            )
        )
        val created = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(customer)
        }
        val id = created.bodyAsText().toInt()

        // Update
        val updatedCustomer =
            Json.encodeToString(CustomerNoAddress(id = id, age = 22, firstName = "Max", lastName = "Maier"))
        val updatedResponse = client.put("/users") {
            contentType(ContentType.Application.Json)
            setBody(updatedCustomer)
        }
        assertEquals(HttpStatusCode.OK, updatedResponse.status)

        // Verify update by reading it again
        val readResponse = client.get("/users/${created.bodyAsText()}") {
            accept(ContentType.Application.Json)
        }
        val expected =
            Json.encodeToString(Customer(id, 22, "Max", "Maier", listOf(Address(id, 8123, "Testing Town", "93a"))))
        assertEquals(HttpStatusCode.OK, readResponse.status)
        assertEquals(expected, readResponse.bodyAsText())
    }

    @Test
    fun testDelete() = testApplication {
        application {
            module(true, false)
        }

        // Create customer
        val customer = Json.encodeToString(
            Customer(
                age = 22, firstName = "Max", lastName = "Mustermann", addresses = listOf(
                    Address(postCode = 8123, city = "Testing Town", houseNumber = "93a")
                )
            )
        )
        val created = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(customer)
        }
        val id = created.bodyAsText().toInt()

        // Delete customer
        val deleteResponse = client.delete("/users/$id")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        // Verify delete by reading it again
        val readResponse = client.get("/users/${created.bodyAsText()}") {
            accept(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.NotFound, readResponse.status)
    }

    @Test
    fun testVerifyCustomerSuccess() = testApplication{
        application {
            module(true, false)
        }

        val customer = Customer(
            age = 22, firstName = "Max", lastName = "Mustermann", addresses = listOf(
                Address(postCode = 8123, city = "Testing Town", houseNumber = "93a")
            )
        )
        // Create customer
        val encoded = Json.encodeToString(customer)
        val created = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(encoded)
        }
        val id = created.bodyAsText().toInt()

        // Test whether the created customer corresponds with the provided one
        val address = Json.encodeToString(customer.addresses.first())
        val comparisonReponse = client.get("/users/verify/$id"){
            contentType(ContentType.Application.Json)
            setBody(address)
        }

        assertEquals(HttpStatusCode.OK, comparisonReponse.status)
        assertEquals("true", comparisonReponse.bodyAsText())
    }

    @Test
    fun testVerifyCustomerFail() = testApplication{
        application {
            module(true, false)
        }

        val customer = Customer(
            age = 22, firstName = "Max", lastName = "Mustermann", addresses = listOf(
                Address(postCode = 8123, city = "Testing Town", houseNumber = "93a")
            )
        )
        val created = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(customer))
        }
        val id = created.bodyAsText().toInt()

        // Test whether the created customer corresponds with the provided one
        // Test whether the created customer corresponds with the provided one
        val address = Json.encodeToString(Address(postCode = 1234, city = "Testing Town", houseNumber = "93a"))
        val comparisonReponse = client.get("/users/verify/$id"){
            contentType(ContentType.Application.Json)
            setBody(address)
        }

        assertEquals(HttpStatusCode.OK, comparisonReponse.status)
        assertEquals("false", comparisonReponse.bodyAsText())
    }

    @Test
    fun testUpdateAddress() = testApplication {
        application {
            module(true, false)
        }

        // Create customer
        val customer = Json.encodeToString(
            Customer(
                age = 22, firstName = "Max", lastName = "Mustermann", addresses = listOf(
                    Address(postCode = 8123, city = "Testing Town", houseNumber = "93a")
                )
            )
        )
        val created = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(customer)
        }
        val id = created.bodyAsText().toInt()

        // Get the address of the created customer
        val readResponse = client.get("/users/$id") {
            accept(ContentType.Application.Json)
        }
        val readCustomer = Json.decodeFromString<Customer>(readResponse.bodyAsText())
        val address = readCustomer.addresses.first()
        val updatedAddress = Json.encodeToString(Address(address.id, 1234, "New Town", "33"))

        val updatedResponse = client.put("/address") {
            contentType(ContentType.Application.Json)
            setBody(updatedAddress)
        }

        assertEquals(HttpStatusCode.OK, updatedResponse.status)

    }
}