package com.oli.customer

import com.oli.address.Address
import com.oli.address.Addresses
import com.oli.persistence.DatabaseFactory
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class CustomerDAOImpl : CustomerDAO {

    private fun resultRowToCustomer(resultRow: ResultRow, addresses: List<Address>): Customer = Customer(
        id = resultRow[Customers.id].value,
        age = resultRow[Customers.age],
        firstName = resultRow[Customers.firstName],
        lastName = resultRow[Customers.lastName],
        addresses = addresses
    )

    private fun resultRowToAddress(resultRow: ResultRow): Address = Address(
        id = resultRow[Addresses.id].value,
        postCode = resultRow[Addresses.postCode],
        city = resultRow[Addresses.city],
        houseNumber = resultRow[Addresses.houseNumber]
    )

    override suspend fun create(customer: Customer): Customer? = DatabaseFactory.dbQuery {
        val createdCustomerId = Customers.insertAndGetId {
            it[age] = customer.age
            it[firstName] = customer.firstName
            it[lastName] = customer.lastName
        }.value
        customer.addresses.forEach { address ->
            Addresses.insert {
                it[customerId] = createdCustomerId
                it[postCode] = address.postCode
                it[city] = address.city
                it[houseNumber] = address.houseNumber
            }
        }
        return@dbQuery readQuery(createdCustomerId)
    }

    override suspend fun read(id: Int): Customer? = DatabaseFactory.dbQuery { readQuery(id) }

    private fun readQuery(customerId: Int): Customer? {
        val addresses = Addresses.select { Addresses.customerId eq customerId }.map(::resultRowToAddress)
        return Customers.select { Customers.id eq customerId }.map { resultRowToCustomer(it, addresses) }.firstOrNull()
    }

    override suspend fun delete(id: Int): Int = DatabaseFactory.dbQuery {
        Addresses.deleteWhere { customerId eq id }
        Customers.deleteWhere { Customers.id eq id }
    }

    override suspend fun update(customer: CustomerNoAddress): Int = DatabaseFactory.dbQuery {
        Customers.update(where = { Customers.id eq customer.id }) {
            it[age] = customer.age
            it[firstName] = customer.firstName
            it[lastName] = customer.lastName
        }
    }
}
