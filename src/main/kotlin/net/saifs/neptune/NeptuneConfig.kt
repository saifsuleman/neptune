package net.saifs.neptune

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class NeptuneConfig {
    var database = DatabaseConfig()
}

@ConfigSerializable
class DatabaseConfig {
    var ip = "localhost"
    var port = 3306
    var username = "root"
    var password = "root"
    var database = "neptune"

    fun jdbc(): String = "jdbc:mysql://$ip:$port/$database"
}