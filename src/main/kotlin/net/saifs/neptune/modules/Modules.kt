package net.saifs.neptune.modules

import net.saifs.neptune.Neptune
import net.saifs.neptune.command.NeptuneCommandManager
import org.bukkit.Server
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.cast

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModuleData(val id: String)

abstract class AbstractModule {
    private val moduleData = resolveData()
    private val logger = LoggerFactory.getLogger("neptune-module:${moduleData.id}")

    val terminables = mutableListOf<() -> Unit>()

    abstract fun init()

    fun logger(): Logger {
        return logger
    }

    fun pluginInstance(): Neptune {
        return Neptune.instance
    }

    fun getId(): String {
        return moduleData.id
    }

    fun onClose(callback: () -> Unit) {
        terminables.add(callback)
    }

    fun <T> config(clazz: Class<T>): T {
        val configManager = Neptune.instance.configManager
        if (!configManager.isConfigLoaded(clazz)) {
            configManager.initConfig(Path.of("configs/${getId()}"), clazz)
        }
        return configManager.getConfig(clazz)
    }

    fun <T: Any> config(clazz: KClass<T>): T = config(clazz.java)

    inline fun <reified T : Any> config(): T = config(T::class)

    private fun resolveData(): ModuleData {
        if (!this.javaClass.isAnnotationPresent(ModuleData::class.java)) {
            throw IllegalArgumentException("Module data is not present!")
        }

        return this.javaClass.getAnnotation(ModuleData::class.java)
    }
}

abstract class NeptuneModule : AbstractModule() {
    val sql = Neptune.instance.sqlWorker
    protected val modules = Neptune.instance.modulesManager
    protected val menus = Neptune.instance.menuManager
    private var commandManager: NeptuneCommandManager? = null

    fun getServer(): Server = pluginInstance().server

    fun registerCommands(): NeptuneCommandManager {
        if (commandManager != null) {
            return commandManager!!
        }

        commandManager = NeptuneCommandManager(this)
        onClose {
            commandManager!!.close()
        }
        return commandManager!!
    }

    inline fun <reified T : Event> subscribe(noinline callback: (T) -> Unit): ModuleEventListener<T> = subscribe(T::class, callback)

    fun <T : Event> subscribe(clazz: KClass<out T>, callback: (T) -> Unit): ModuleEventListener<T> {
        val listener = ModuleEventListener(clazz, callback)
        val pluginManager = getServer().pluginManager
        pluginManager.registerEvent(clazz.java, listener, EventPriority.NORMAL, listener, pluginInstance())
        onClose(listener::unregister)
        return listener
    }
}

class ModuleEventListener<T : Event>(val clazz: KClass<out T>, private val callback: (T) -> Unit) : Listener,
    EventExecutor {
    override fun execute(listener: Listener, event: Event) {
        if (clazz.isInstance(event)) {
            callback.invoke(clazz.cast(event))
        }
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
    }
}