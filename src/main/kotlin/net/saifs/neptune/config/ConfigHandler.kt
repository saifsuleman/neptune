package net.saifs.neptune.config

import org.slf4j.LoggerFactory
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.reference.ConfigurationReference
import org.spongepowered.configurate.reference.ValueReference
import org.spongepowered.configurate.reference.WatchServiceListener
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class ConfigHandler<T>(applicationFolder: Path, configName: String, private val clazz: Class<T>) : AutoCloseable {
    val listener = WatchServiceListener.create()
    lateinit var base: ConfigurationReference<BasicConfigurationNode>
    lateinit var config: ValueReference<T, BasicConfigurationNode>

    val configFile: Path = Paths.get(applicationFolder.toString() + File.separator + configName)

    init {
        val configFile = configFile.toFile()
        if (!configFile.exists()) {
            if (!configFile.parentFile.exists()) {
                configFile.parentFile.mkdirs()
            }
        }

        try {
            base = this.listener.listenToConfiguration(
                { file ->
                    GsonConfigurationLoader.builder()
                        .defaultOptions { it.shouldCopyDefaults(true) }
                        .path(file)
                        .build()
                }, this.configFile
            )

            this.listener.listenToFile(this.configFile) {}

            this.config = this.base.referenceTo(clazz)
            this.base.save()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

    }

    fun getConfig(): T {
        return this.config.get()!!
    }

    fun saveToFile() {
        this.base.node().set(clazz, clazz.cast(getConfig()))
        this.base.loader().save(this.base.node())
    }

    override fun close() {
        this.listener.close()
        this.base.close()
    }
}