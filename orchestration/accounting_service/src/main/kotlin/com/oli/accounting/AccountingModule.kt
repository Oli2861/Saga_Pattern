package com.oli.accounting

import com.oli.creditcardinfo.CreditCardInfoDAO
import com.oli.creditcardinfo.CreditCardInfoDAOImpl
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.koin
import org.slf4j.LoggerFactory

fun Application.accountingModule(){
    koin{
        modules(accountingKoinModule)
    }
    accountingRouting()
}

val accountingKoinModule = module{
    single { AccountingService(logger = get(), creditCardInfoDAO = get()) }
    single<CreditCardInfoDAO> { CreditCardInfoDAOImpl() }
}