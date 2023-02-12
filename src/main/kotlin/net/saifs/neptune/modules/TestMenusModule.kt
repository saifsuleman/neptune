package net.saifs.neptune.modules

import cloud.commandframework.annotations.CommandMethod
import net.saifs.neptune.core.extensions.stackOf
import net.saifs.neptune.core.modules.ModuleData
import net.saifs.neptune.core.modules.NeptuneModule
import net.saifs.neptune.util.parseMini
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@ModuleData("test-menus")
class TestMenusModule : NeptuneModule() {
    override fun init() {
        registerCommands().registerHelp("testmenu")
    }

    @CommandMethod("testmenu")
    fun test(player: Player) {
        val menu = menus.createMenu {
            "Testing Menu"
        }

        menu.newPartition("111111111") { partition ->
            partition.newSlot(ItemStack(Material.DIAMOND))
            for (i in 0..7) {
                partition.newSlot(stackOf(Material.STICK) {
                    amount(1)
                    name("<#ff70dd><bold>Hello")
                    lore(listOf(
                        "<#ff0000>hllo there"
                    ).map(::parseMini))
                }) { player, clickType ->
                    player.closeInventory()
                    player.sendMessage("hello")
                }
            }
        }

        menu.open(player)
    }
}