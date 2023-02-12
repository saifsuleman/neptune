package net.saifs.neptune

import net.saifs.neptune.core.gui.MenuSystem
import net.saifs.neptune.core.modules.ModulesManager
import net.saifs.neptune.core.sql.HikariDatabaseConnector
import net.saifs.neptune.core.sql.SQLWorker
import net.saifs.neptune.modules.EconomyModule
import net.saifs.neptune.modules.TestMenusModule
import org.bukkit.plugin.java.JavaPlugin

class Neptune : JavaPlugin() {
    lateinit var modulesManager: ModulesManager
        private set
    lateinit var sqlWorker: SQLWorker
        private set

    lateinit var menuSystem: MenuSystem
        private set

    companion object {
        lateinit var instance: Neptune
            private set
    }

    override fun onEnable() {
        instance = this
        menuSystem = MenuSystem()
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
        modulesManager.load<TestMenusModule>()
    }

    override fun onDisable() {
        modulesManager.close()
    }
}