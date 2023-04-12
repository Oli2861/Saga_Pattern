package com.oli.address

import com.oli.persistence.DatabaseFactory
import org.jetbrains.exposed.sql.update

class AddressDAOImpl: AddressDAO {
    override suspend fun update(address: Address): Int = DatabaseFactory.dbQuery{
        Addresses.update(where = {Addresses.id eq address.id}){
            it[postCode] = address.postCode
            it[city] = address.city
            it[houseNumber] = address.houseNumber
        }
    }

}