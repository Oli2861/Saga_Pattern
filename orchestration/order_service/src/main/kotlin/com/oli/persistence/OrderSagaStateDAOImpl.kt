package com.oli.persistence

import com.oli.saga.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class CreateOrderSagaStateDAOImpl : CreateOrderSagaStateDAO {

    private fun resultRowToCreateOrderSagaState(resultRow: ResultRow) = CreateOrderSagaState(
        sagaId = resultRow[CreateOrderSagaStates.id].value,
        currentStep = resultRow[CreateOrderSagaStates.currentStep],
        rollingBack = resultRow[CreateOrderSagaStates.rollingBack],
        orderId = resultRow[CreateOrderSagaStates.orderId]?.value
    )

    override suspend fun create(
        createOrderSagaState: CreateOrderSagaState
    ): CreateOrderSagaState? =
        DatabaseFactory.dbQuery {
            val id = CreateOrderSagaStates.insertAndGetId {
                it[currentStep] = createOrderSagaState.currentStep
                it[rollingBack] = createOrderSagaState.rollingBack
                it[orderId] = createOrderSagaState.orderId

            }.value
            return@dbQuery readQuery(id)
        }

    private fun readQuery(id: Int): CreateOrderSagaState? {
        return CreateOrderSagaStates.select { CreateOrderSagaStates.id eq id }.map(::resultRowToCreateOrderSagaState)
            .firstOrNull()
    }


    override suspend fun read(id: Int): CreateOrderSagaState? = DatabaseFactory.dbQuery { readQuery(id) }

    override suspend fun delete(id: Int): Int = DatabaseFactory.dbQuery {
        CreateOrderSagaStates.deleteWhere { CreateOrderSagaStates.id eq id }
    }

    override suspend fun update(sagaState: SagaState): Int = DatabaseFactory.dbQuery {
        CreateOrderSagaStates.update({ CreateOrderSagaStates.id eq sagaState.sagaId }) {
            it[currentStep] = sagaState.currentStep
            it[rollingBack] = sagaState.rollingBack
        }
    }


}