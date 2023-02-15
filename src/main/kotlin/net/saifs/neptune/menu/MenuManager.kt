package net.saifs.neptune.menu

import net.saifs.neptune.Neptune
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import kotlin.math.ceil
import kotlin.math.min

class MenuManager : Listener {
    init {
        val listener = MenuListener()
        Neptune.instance.server.pluginManager.registerEvents(listener, Neptune.instance)
    }

    fun newMenu(size: Int = 54, block: MenuBuilder.() -> Unit) = Menu(size, block)
}

class Menu internal constructor(val size: Int = 54, val block: MenuBuilder.() -> Unit) {
    private val refreshing: MutableSet<Player> = mutableSetOf()

    fun open(player: Player) {
        player.openInventory(build())
    }

    fun refresh(player: Player) {
        refreshing.add(player)
        open(player)
        refreshing.remove(player)
    }

    fun isRefreshing(player: Player): Boolean {
        return refreshing.contains(player)
    }

    private fun build(): Inventory {
        val builder = MenuBuilder()
        builder.apply(block)
        val rows: Int = ceil(min(builder.size(), (builder.items.keys.reduceOrNull(Math::max) ?: -1) + 1) / 9.0).toInt()
        val holder = MenuHolder(this, builder)
        val title = builder.title()
        val inventory = if (title != null) {
            Bukkit.createInventory(holder, rows * 9, title)
        } else {
            Bukkit.createInventory(holder, rows * 9)
        }
        holder.inv = inventory
        for ((index, item) in builder.items) {
            inventory.setItem(index, item.item)
        }
        return inventory
    }
}

class MenuHolder(val menu: Menu, val builder: MenuBuilder) : InventoryHolder {
    lateinit var inv: Inventory

    override fun getInventory(): Inventory {
        return this.inv
    }
}

class MenuListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        if (inventory.holder !is MenuHolder) {
            return
        }
        event.isCancelled = true
        val holder = inventory.holder as MenuHolder
        val clicked = holder.builder.items[event.slot] ?: return
        val handler = clicked.handler ?: return
        handler(MenuClickData(event.whoClicked as Player, event.click))
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val inventory = event.inventory
        if (inventory.holder !is MenuHolder) {
            return
        }
        val player = event.player as Player
        val holder = inventory.holder as MenuHolder

        if (holder.menu.isRefreshing(player)) {
            return
        }

        holder.builder.terminables.forEach { it(player) }
    }
}