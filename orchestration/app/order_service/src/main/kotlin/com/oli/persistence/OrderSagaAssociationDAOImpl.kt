package com.oli.persistence

import com.oli.order.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class OrderSagaAssociationDAOImpl : OrderSagaAssociationDAO {
    private fun resultRowToOrderSagaAssociation(resultRow: ResultRow) = OrderSagaAssociation(
        orderId = resultRow[OrderSagaAssociations.orderId].value,
        sagaId = resultRow[OrderSagaAssociations.sagaId].value
    )

    override suspend fun create(orderID: Int, sagaStateId: Int): OrderSagaAssociation? = DatabaseFactory.dbQuery {
        OrderSagaAssociations.insertAndGetId {
            it[orderId] = orderID
            it[sagaId] = sagaStateId
        }
        return@dbQuery readQuery(orderID, sagaStateId)
    }

    private fun readQuery(orderID: Int, sagaID: Int): OrderSagaAssociation? {
        return OrderSagaAssociations.select {
            (OrderSagaAssociations.orderId eq orderID) and (OrderSagaAssociations.sagaId eq sagaID)
        }.map(::resultRowToOrderSagaAssociation).firstOrNull()
    }

    override suspend fun readUsingSagaId(sagaId: Int): OrderSagaAssociation? = DatabaseFactory.dbQuery {
        OrderSagaAssociations.select { OrderSagaAssociations.sagaId eq sagaId }.map(::resultRowToOrderSagaAssociation).firstOrNull()
    }

    override suspend fun deleteAllForSaga(sagaId: Int): Int = DatabaseFactory.dbQuery {
        OrderSagaAssociations.deleteWhere { this.sagaId eq sagaId }
    }

    override suspend fun deleteAllForOrder(orderId: Int): Int = DatabaseFactory.dbQuery {
        OrderSagaAssociations.deleteWhere { this.orderId eq orderId }
    }

}