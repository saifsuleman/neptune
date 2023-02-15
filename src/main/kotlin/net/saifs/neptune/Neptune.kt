package net.saifs.neptune

import net.saifs.neptune.menu.MenuManager
import net.saifs.neptune.modules.ModulesManager
import net.saifs.neptune.sql.HikariDatabaseConnector
import net.saifs.neptune.sql.SQLWorker
import org.bukkit.plugin.java.JavaPlugin

class Neptune : JavaPlugin() {
    lateinit var modulesManager: ModulesManager
    lateinit var sqlWorker: SQLWorker
    lateinit var menuManager: MenuManager

    companion object {
        lateinit var instance: Neptune
    }

    override fun onEnable() {
        instance = this
        menuManager = MenuManager()
        modulesManager = ModulesManager(this)

        saveDefaultConfig()
        config.options().copyDefaults(true)

        val ip = config.getString("db.ip")!!
        val port = config.getInt("db.port")
        val username = config.getString("db.username")!!
        val password = config.getString("db.password")!!
        val database = config.getString("db.database")!!

        sqlWorker = SQLWorker(HikariDatabaseConnector("jdbc:mysql://$ip:$port/$database", username, password))
    }

    override fun onDisable() {
        modulesManager.close()
    }
}