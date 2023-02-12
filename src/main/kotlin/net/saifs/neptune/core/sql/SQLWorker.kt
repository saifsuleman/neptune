package net.saifs.neptune.core.sql

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.concurrent.CompletableFuture

class SQLWorker(private val connector: HikariDatabaseConnector) {
    fun query(query: String): CompletableFuture<ResultSet> = CompletableFuture.supplyAsync {
        connector.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.executeQuery()
            }
        }
    }

    fun query(query: String, statementConsumer: (PreparedStatement) -> Unit): CompletableFuture<ResultSet> =
        CompletableFuture.supplyAsync {
            connector.getConnection().use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statementConsumer(statement)
                    statement.executeQuery()
                }
            }
        }

    fun query(query: String, params: List<Any>): CompletableFuture<ResultSet> = query(query) {
        populateStatement(it, params)
    }

    fun update(query: String): CompletableFuture<Int> = CompletableFuture.supplyAsync {
        connector.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.executeUpdate()
            }
        }
    }

    fun update(query: String, statementConsumer: (PreparedStatement) -> Unit): CompletableFuture<Int> =
        CompletableFuture.supplyAsync {
            connector.getConnection().use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statementConsumer(statement)
                    statement.executeUpdate()
                }
            }
        }

    fun update(query: String, params: List<Any>): CompletableFuture<Int> = update(query) {
        populateStatement(it, params)
    }

    fun updateBatch(query: String, statementConsumer: (PreparedStatement) -> Unit): CompletableFuture<Unit> = CompletableFuture.supplyAsync {
        connector.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statementConsumer(statement)
                statement.executeBatch()
            }
        }
    }

    private fun populateStatement(statement: PreparedStatement, params: List<Any>) {
        for (i in 0..params.size) {
            statement.setObject(i + 1, params[i])
        }
    }
}
