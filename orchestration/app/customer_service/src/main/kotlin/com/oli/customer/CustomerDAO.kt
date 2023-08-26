package com.oli.customer

interface CustomerDAO {
    suspend fun create(customer: Customer): Customer?
    suspend fun read(id: Int): Customer?
    suspend fun delete(id: Int): Int
    suspend fun update(customer: CustomerNoAddress): Int
}
