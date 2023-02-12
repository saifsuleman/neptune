package net.saifs.neptune.modules

import net.saifs.neptune.Neptune
import net.saifs.neptune.util.broadcast
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.cast

class ModulesManager(private val plugin: Neptune) {
    private val modules: MutableMap<KClass<out NeptuneModule>, NeptuneModule> = mutableMapOf()

    inline fun <reified T> get(): T where T : NeptuneModule = get(T::class)

    fun get(id: String): NeptuneModule? {
        for (module in modules.values) {
            if (module.getId() == id) {
                return module
            }
        }
        return null
    }

    fun <T> get(clazz: KClass<T>): T where T : NeptuneModule {
        if (modules.containsKey(clazz)) {
            val value = modules[clazz]
            if (value != null) {
                return clazz.cast(value)
            }
        }

        return load(clazz)
    }

    fun <T> load(module: T): T where T : NeptuneModule {
        val clazz = module::class

        if (modules.containsKey(clazz)) {
            unload(clazz)
        }

        modules[clazz] = module
        module.init()

        broadcast("<#426ff5>Loaded module <#b6bbcc>" + module.getId(), "neptune.admin")

        return module
    }

    fun <T> load(clazz: KClass<T>): T where T : NeptuneModule {
        if (modules.containsKey(clazz)) {
            val value = modules[clazz]
            if (value != null) {
                return clazz.cast(value)
            }
        }

        val constructor = clazz.java.getConstructor()
        val instance = constructor.newInstance()
        return load(instance)
    }

    inline fun <reified T> load(): T where T : NeptuneModule = load(T::class)

    fun <T> unload(clazz: KClass<T>): T where T : NeptuneModule {
        val module = modules.remove(clazz) ?: throw IllegalArgumentException("Module not loaded $clazz")

        for (terminable in module.terminables) {
            terminable()
        }

        module.terminables.clear()

        broadcast("<#426ff5>Unloaded module <#b6bbcc>" + module.getId(), "neptune.admin")

        return clazz.cast(module)
    }

    fun <T : NeptuneModule> isLoaded(clazz: KClass<T>): Boolean {
        return modules.containsKey(clazz)
    }

    fun <T : NeptuneModule> isLoaded(module: T): Boolean = isLoaded(module::class)

    inline fun <reified T> unload(): T where T : NeptuneModule = unload(T::class)

    fun <T : NeptuneModule> unload(module: T): T = unload(module::class)

    internal fun close() = modules.values.forEach(::unload)
}