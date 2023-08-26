package com.oli.proxies

import kotlinx.serialization.Serializable
import com.oli.types.Address

@Serializable
data class Customer(
    val id: Int = 0,
    val age: Int,
    val firstName: String,
    val lastName: String,
    @Serializable
    val addresses: List<Address>
)