package net.saifs.neptune.menu

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

data class MenuItem(val item: ItemStack, val handler: MenuClickHandler? = null)

data class MenuClickData(val player: Player, val clickType: ClickType)

typealias MenuClickHandler = (MenuClickData) -> Unit