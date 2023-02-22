package net.saifs.neptune

import net.saifs.neptune.command.NeptuneCommandManager
import net.saifs.neptune.config.ConfigManager
import net.saifs.neptune.menu.MenuManager
import net.saifs.neptune.sql.SQLWorker
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.cast

abstract class NeptunePlugin : JavaPlugin() {
    lateinit var menus: MenuManager
    lateinit var configManager: ConfigManager
    var sql: SQLWorker? = null
    var commandManager: NeptuneCommandManager? = null

    private val terminables = mutableListOf<() -> Unit>()

    fun onClose(callback: () -> Unit) {
        terminables.add(callback)
    }

    fun <T> config(clazz: Class<T>): T {
        if (!configManager.isConfigLoaded(clazz)) {
            configManager.initConfig(Path.of("configs/${this.name}"), clazz)
        }
        return configManager.getConfig(clazz)
    }

    fun <T: Any> config(clazz: KClass<T>): T = config(clazz.java)

    inline fun <reified T : Any> config(): T = config(T::class)

    fun <T : Event> subscribe(clazz: KClass<out T>, callback: (T) -> Unit): ModuleEventListener<T> {
        val listener = ModuleEventListener(clazz, callback)
        server.pluginManager.registerEvent(clazz.java, listener, EventPriority.NORMAL, listener, this)
        return listener
    }

    abstract fun init()

    override fun onEnable() {
        menus = MenuManager(this)
        configManager = ConfigManager()

        init()

        commandManager?.syncCommands()
    }

    override fun onDisable() {
        for (terminable in terminables) {
            terminable()
        }

        terminables.clear()

        configManager.close()
        commandManager?.close()
    }

    inline fun <reified T : Any> registerCommands() = registerCommands(T::class)

    fun <T : Any> registerCommands(vararg classes: KClass<T>) {
        for (clazz in classes) {
            if (commandManager == null) {
                commandManager = NeptuneCommandManager(this)
            }

            commandManager!!.registerCommands(clazz.java)
        }
    }
}

class ModuleEventListener<T : Event>(val clazz: KClass<out T>, private val callback: (T) -> Unit) : Listener,
    EventExecutor {
    override fun execute(listener: Listener, event: Event) {
        if (clazz.isInstance(event)) {
            callback.invoke(clazz.cast(event))
        }
    }
}