package net.saifs.neptune.config

import com.google.common.collect.Maps
import org.spongepowered.configurate.ConfigurateException
import java.nio.file.Path
import kotlin.reflect.KClass

class ConfigManager : AutoCloseable {
    companion object {
        private val CONFIGS: MutableMap<Class<*>, ConfigHandler<*>> = Maps.newConcurrentMap()
    }

    override fun close() {
        for (configHandler in CONFIGS.values) {
            try {
                configHandler.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveConfig(config: Class<*>) {
        try {
            CONFIGS[config]!!.saveToFile()
        } catch (e: ConfigurateException) {
            e.printStackTrace()
        }
    }

    fun saveConfig(config: KClass<*>) = saveConfig(config.java)

    fun initConfig(dir: Path, config: Class<*>) {
        val fileName = config.simpleName.lowercase() + ".json"
        CONFIGS[config] = ConfigHandler(dir, fileName, config)
    }

    fun isConfigLoaded(config: KClass<*>): Boolean = isConfigLoaded(config.java)

    fun isConfigLoaded(config: Class<*>): Boolean {
        return CONFIGS.containsKey(config)
    }

    fun initConfig(dir: Path, config: KClass<*>) {
        initConfig(dir, config.java)
    }

    fun <T> getConfig(config: Class<T>): T {
        return config.cast(CONFIGS[config]!!.getConfig())
    }
}