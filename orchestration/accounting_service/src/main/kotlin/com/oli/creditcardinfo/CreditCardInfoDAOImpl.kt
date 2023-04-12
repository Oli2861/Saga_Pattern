package com.oli.creditcardinfo

import com.oli.persistence.DatabaseFactory
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

class CreditCardInfoDAOImpl : CreditCardInfoDAO {

    private fun resultRowToCreditCardInfo(resultRow: ResultRow): CreditCardInfo = CreditCardInfo(
        userId = resultRow[CreditCardInfos.userId],
        info = resultRow[CreditCardInfos.info]
    )

    override suspend fun create(creditCardInfo: CreditCardInfo): CreditCardInfo? = DatabaseFactory.dbQuery {
        val id = CreditCardInfos.insertAndGetId {
            it[userId] = creditCardInfo.userId
            it[info] = creditCardInfo.info
        }.value
        return@dbQuery getQuery(id)
    }

    private fun getQuery(id: Int): CreditCardInfo? {
        return CreditCardInfos.select(CreditCardInfos.id eq id).map(::resultRowToCreditCardInfo).firstOrNull()
    }

    override suspend fun get(customerId: Int): List<CreditCardInfo> = DatabaseFactory.dbQuery {
        return@dbQuery CreditCardInfos.select(CreditCardInfos.userId eq customerId).map(::resultRowToCreditCardInfo)
    }

}
