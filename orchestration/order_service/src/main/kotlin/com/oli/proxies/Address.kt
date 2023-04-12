package com.oli.proxies

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val postCode: Int,
    val city: String,
    val houseNumber: String
)