package net.saifs.neptune.core.modules

import net.saifs.neptune.Neptune
import net.saifs.neptune.core.command.NeptuneCommandManager
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.craftbukkit.v1_19_R2.CraftServer
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.scheduler.BukkitTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
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
    protected var commandManager: NeptuneCommandManager? = null

    fun getServer(): Server = pluginInstance().server

    fun runTaskTimer(delay: Long, period: Long, callback: () -> Unit): BukkitTask {
        val runnable = getServer().scheduler.runTaskTimer(pluginInstance(), callback, delay, period)
        onClose {
            if (!runnable.isCancelled) {
                runnable.cancel()
            }
        }
        return runnable
    }

    fun runTaskTimerAsync(delay: Long, period: Long, callback: () -> Unit): BukkitTask {
        val runnable = getServer().scheduler.runTaskTimerAsynchronously(pluginInstance(), callback, delay, period)
        onClose {
            if (!runnable.isCancelled) {
                runnable.cancel()
            }
        }
        return runnable
    }

    fun registerCommands(): NeptuneCommandManager {
        if (commandManager != null) {
            return commandManager!!
        }

        commandManager = NeptuneCommandManager(this)
        (Bukkit.getServer() as CraftServer).syncCommands()
        onClose {
            commandManager!!.close()
        }
        return commandManager!!
    }

    fun runTaskTimer(period: Long, callback: () -> Unit): BukkitTask = runTaskTimer(0, period, callback)

    fun runTaskTimer(callback: () -> Unit): BukkitTask = runTaskTimer(0, 0, callback)

    fun runTaskTimerAsync(period: Long, callback: () -> Unit): BukkitTask = runTaskTimerAsync(0, period, callback)

    fun runTaskTimerAsync(callback: () -> Unit): BukkitTask = runTaskTimerAsync(0, 0, callback)

    fun runTask(callback: () -> Unit): BukkitTask {
        val runnable = getServer().scheduler.runTask(pluginInstance(), callback)
        onClose {
            if (!runnable.isCancelled) {
                runnable.cancel()
            }
        }
        return runnable
    }

    fun runTaskAsync(callback: () -> Unit): BukkitTask {
        val runnable = getServer().scheduler.runTaskAsynchronously(pluginInstance(), callback)
        onClose {
            if (!runnable.isCancelled) {
                runnable.cancel()
            }
        }
        return runnable
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