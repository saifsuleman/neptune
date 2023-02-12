package net.saifs.neptune.modules

import kotlinx.coroutines.runBlocking
import net.saifs.neptune.core.modules.ModuleData
import net.saifs.neptune.core.modules.NeptuneModule
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.util.UUID
import kotlinx.coroutines.async
import net.saifs.neptune.core.scheduling.SynchronizationContext
import net.saifs.neptune.core.scheduling.schedule

const val DEFAULT_BALANCE = 200

@ModuleData("currencies")
class CurrenciesModule : NeptuneModule() {
    private val currencies = mutableMapOf<String, Currency>()

    override fun init() {
        runBlocking {
            sql.update("""
                CREATE TABLE IF NOT EXISTS currencies (
                    id INT(10) PRIMARY KEY AUTO_INCREMENT,
                    currency VARCHAR(255) NOT NULL,
                    uuid CHAR(36) NOT NULL,
                    balance FLOAT(10) NOT NULL,
                    UNIQUE INDEX (currency, uuid)
                )
            """.trimIndent())
        }
        subscribe<AsyncPlayerPreLoginEvent> { runBlocking { loadPlayer(it.uniqueId) } }
    }

    private suspend fun loadPlayer(uuid: UUID) {
        currencies.values.forEach { it.loadPlayer(uuid) }
    }

    fun newCurrency(name: String): Currency {
        val currency = currencies.computeIfAbsent(name) { Currency(name, this) }

        schedule(SynchronizationContext.ASYNC) {
            Bukkit.getOnlinePlayers().forEach {
                currency.loadPlayer(it.uniqueId)
            }
        }
        return currency
    }
}

class Currency internal constructor(val name: String, private val module: CurrenciesModule) {
    private val data: MutableMap<UUID, Float> = mutableMapOf()

    suspend fun loadPlayer(uuid: UUID) {
        if (data.containsKey(uuid)) {
            return
        }
        val results = module.sql.query("SELECT balance FROM currencies WHERE uuid = ? AND currency = ?") {
            it.setString(1, uuid.toString())
            it.setString(2, name)
        }

        if (!results.next()) {
            return
        }

        data[uuid] = results.getFloat(1)
    }

    suspend fun getLeaderboard(start: Int, end: Int): List<Pair<UUID, Float>> {
        val results = module.sql.query("SELECT uuid, balance FROM currencies WHERE currency = ?")
        return mutableListOf()
    }

    operator fun get(player: Player): Float = get(player.uniqueId)

    operator fun set(player: Player, amount: Float) = set(player.uniqueId, amount)

    operator fun get(uuid: UUID): Float {
        return data[uuid] ?: 0f
    }

    operator fun set(uuid: UUID, amount: Float) {
        data[uuid] = amount
        module.schedule(SynchronizationContext.ASYNC) {
            module.sql.update("INSERT INTO currencies (currency, uuid, balance) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE balance = VALUES(balance)") {
                it.setString(1, name)
                it.setString(2, uuid.toString())
                it.setFloat(3, amount)
            }
        }
    }
}