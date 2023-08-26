package com.oli.types

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val postCode: Int,
    val city: String,
    val houseNumber: String,
    val id: Int = 0
)
