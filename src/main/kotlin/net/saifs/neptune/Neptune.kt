package net.saifs.neptune

import net.saifs.neptune.core.modules.ModulesManager
import net.saifs.neptune.core.sql.HikariDatabaseConnector
import net.saifs.neptune.core.sql.SQLWorker
import net.saifs.neptune.modules.EconomyModule
import org.bukkit.plugin.java.JavaPlugin

class Neptune : JavaPlugin() {
    lateinit var modulesManager: ModulesManager
        private set
    lateinit var sqlWorker: SQLWorker
        private set

    companion object {
        lateinit var instance: Neptune
            private set
    }

    override fun onEnable() {
        instance = this
        modulesManager = ModulesManager(this)

        saveDefaultConfig()
        config.options().copyDefaults(true)

        val ip = config.getString("db.ip")!!
        val port = config.getInt("db.port")
        val username = config.getString("db.username")!!
        val password = config.getString("db.password")!!
        val database = config.getString("db.database")!!

        sqlWorker = SQLWorker(HikariDatabaseConnector("jdbc:mysql://$ip:$port/$database", username, password))

        modulesManager.load<EconomyModule>()
    }

    override fun onDisable() {
        modulesManager.close()
    }
}