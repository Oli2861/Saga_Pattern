package com.oli.creditcardinfo

interface CreditCardInfoDAO {
    suspend fun create(creditCardInfo: CreditCardInfo): CreditCardInfo?
    suspend fun get(customerId: Int): List<CreditCardInfo>
}