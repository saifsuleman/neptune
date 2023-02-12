package net.saifs.neptune.sql

import kotlinx.coroutines.suspendCancellableCoroutine
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SQLWorker(private val connector: HikariDatabaseConnector) {
    private suspend fun <T> execute(provider: () -> T): T {
        return suspendCancellableCoroutine { cont ->
            runCatching(provider).fold(cont::resume, cont::resumeWithException)
        }
    }

    suspend fun query(query: String): ResultSet = execute {
        connector.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                return@execute statement.executeQuery()
            }
        }
    }

    suspend fun query(query: String, statementConsumer: (PreparedStatement) -> Unit): ResultSet = execute {
        connector.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statementConsumer(statement)
                return@execute statement.executeQuery()
            }
        }
    }

    suspend fun query(query: String, params: List<Any>): ResultSet = query(query) {
        populateStatement(it, params)
    }

    suspend fun update(query: String): Int = execute {
        connector.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.executeUpdate()
            }
        }
    }

    suspend fun update(query: String, statementConsumer: (PreparedStatement) -> Unit): Int = execute {
            connector.getConnection().use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statementConsumer(statement)
                    statement.executeUpdate()
                }
            }
        }

    suspend fun update(query: String, params: List<Any>): Int = update(query) {
        populateStatement(it, params)
    }

    suspend fun updateBatch(query: String, statementConsumer: (PreparedStatement) -> Unit): Unit = execute {
            connector.getConnection().use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statementConsumer(statement)
                    statement.executeBatch()
                }
            }
        }

    private fun populateStatement(statement: PreparedStatement, params: List<Any>) {
        for (i in params.indices) {
            statement.setObject(i + 1, params[i])
        }
    }
}
