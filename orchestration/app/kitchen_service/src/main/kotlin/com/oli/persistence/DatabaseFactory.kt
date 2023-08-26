package com.oli.persistence

import com.oli.ticket.OrderItems
import com.oli.ticket.Tickets
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object DatabaseFactory {

    private val logger: Logger = LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun init(useEmbeddedDatabase: Boolean) {
        val database = if (useEmbeddedDatabase) connectToH2() else connectToPostgres()

        transaction(database) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Tickets)
            SchemaUtils.create(OrderItems)
        }
    }

    private fun connectToPostgres(): Database {
        val host = System.getenv("POSTGRES_HOST")
        val port = System.getenv("POSTGRES_PORT")
        val user = System.getenv("POSTGRES_USER")
        val password = System.getenv("POSTGRES_PASSWORD")
        val databaseName = System.getenv("POSTGRES_DB")
        logger.debug("Connecting to jdbc:postgresql://$host:$port/$databaseName?user=$user&password=$password")
        return Database.connect(
            url = "jdbc:postgresql://$host:$port/$databaseName?user=$user&password=$password",
            driver = "org.postgresql.Driver"
        )
    }

    private fun connectToH2(): Database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = ""
    )


    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}