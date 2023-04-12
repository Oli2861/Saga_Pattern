package com.oli.address

import com.oli.address.Addresses.references
import com.oli.customer.Customers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class Address(
    val id: Int = 0,
    val postCode: Int,
    val city: String,
    val houseNumber: String
) {

    fun equalsIgnoreId(other: Any?): Boolean {
        if (other !is Address) return false
        if (other.postCode != postCode) return false
        if (other.city != city) return false
        if (other.houseNumber != houseNumber) return false
        return true
    }
}

fun List<Address>.containsEqualNoId(address: Address): Boolean {
    forEach { curr ->
        if (curr.equalsIgnoreId(address)) return true
    }
    return false
}

object Addresses : IntIdTable() {
    val customerId = reference("customerId", Customers)
    val postCode = integer("postCode")
    val city = varchar("city", 64)
    val houseNumber = varchar("houseNumber", 16)
}