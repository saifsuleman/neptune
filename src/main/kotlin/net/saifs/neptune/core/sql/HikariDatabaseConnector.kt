package net.saifs.neptune.core.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.TimeUnit


class HikariDatabaseConnector(jdbcUrl: String, username: String, password: String) : AutoCloseable {
    private val dataSource: HikariDataSource

    init {
        val config = HikariConfig()

        config.jdbcUrl = jdbcUrl;
        config.addDataSourceProperty("user", username);
        config.addDataSourceProperty("password", password);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("cacheCallableStmts", true);
        config.addDataSourceProperty("alwaysSendSetIsolation", false);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("maxLifetime", TimeUnit.MINUTES.toMillis(10));
        config.connectionTimeout = TimeUnit.SECONDS.toMillis(15);
        config.leakDetectionThreshold = TimeUnit.SECONDS.toMillis(10);

        this.dataSource = HikariDataSource(config)
    }

    fun getConnection(): Connection = dataSource.connection

    override fun close() {
        this.dataSource.close()
    }
}