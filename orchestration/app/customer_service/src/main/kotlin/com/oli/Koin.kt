package com.oli

import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory

fun Application.configureKoin(){
    install(Koin){
        slf4jLogger()
        modules(appKoinModule)
    }
}

private val appKoinModule = module {
    single {
        LoggerFactory.getLogger("")
    }
}
