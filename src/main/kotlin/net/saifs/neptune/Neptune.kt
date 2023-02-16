package net.saifs.neptune

import net.saifs.neptune.config.ConfigManager
import net.saifs.neptune.extensions.stackOf
import net.saifs.neptune.menu.MenuManager
import net.saifs.neptune.modules.ModulesManager
import net.saifs.neptune.modulestest.EconomyModule
import net.saifs.neptune.sql.HikariDatabaseConnector
import net.saifs.neptune.sql.SQLWorker
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path

class Neptune : JavaPlugin() {
    lateinit var modulesManager: ModulesManager
    lateinit var sqlWorker: SQLWorker
    lateinit var menuManager: MenuManager
    lateinit var configManager: ConfigManager

    companion object {
        lateinit var instance: Neptune
    }

    override fun onEnable() {
        instance = this
        menuManager = MenuManager()
        configManager = ConfigManager()
        modulesManager = ModulesManager(this)

        configManager.initConfig(Path.of("configs/"), NeptuneConfig::class)
        val database = configManager.getConfig(NeptuneConfig::class.java).database

        sqlWorker = SQLWorker(HikariDatabaseConnector(database.jdbc(), database.username, database.password))
    }

    override fun onDisable() {
        configManager.close()
        modulesManager.close()
    }
}