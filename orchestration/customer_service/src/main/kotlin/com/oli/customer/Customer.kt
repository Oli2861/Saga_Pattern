package com.oli.customer

import com.oli.address.Address
import com.oli.address.containsEqualNoId
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class Customer(
    val id: Int = 0,
    val age: Int,
    val firstName: String,
    val lastName: String,
    @Serializable
    val addresses: List<Address>
) {

    /**
     * Checks whether two customers are considered to be equal.
     */
    fun equalIgnoreId(other: Any?): Boolean {
        if (!equalIgnoreIdAndAddresses(other)) return false
        if (other !is Customer) return false
        if (other.addresses.size != addresses.size) return false
        if (!other.addresses.all { addresses.containsEqualNoId(it) }) return false
        return true
    }

    fun equalIgnoreIdAndAddresses(other: Any?): Boolean {
        if (other !is Customer) return false
        if (other.age != age) return false
        if (other.firstName != firstName) return false
        if (other.lastName != lastName) return false
        return true
    }
}

@Serializable
data class CustomerNoAddress(
    val id: Int = 0,
    val age: Int,
    val firstName: String,
    val lastName: String
)

object Customers : IntIdTable() {
    val age = integer("age")
    val firstName = varchar("firstName", 128)
    val lastName = varchar("lastName", 128)
}