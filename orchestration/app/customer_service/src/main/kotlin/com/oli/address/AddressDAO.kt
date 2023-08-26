package com.oli.address

interface AddressDAO {
    suspend fun update(address: Address): Int
}