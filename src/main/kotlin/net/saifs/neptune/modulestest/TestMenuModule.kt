package net.saifs.neptune.modulestest

import cloud.commandframework.annotations.CommandMethod
import net.saifs.neptune.extensions.stackOf
import net.saifs.neptune.modules.ModuleData
import net.saifs.neptune.modules.NeptuneModule
import net.saifs.neptune.scheduling.SynchronizationContext
import net.saifs.neptune.scheduling.schedule
import net.saifs.neptune.util.parseMini
import org.bukkit.Material
import org.bukkit.entity.Player

@ModuleData("test-menus")
class TestMenuModule : NeptuneModule() {
    private val menu = menus.newMenu {
        size(27)

        title("<rainbow>Hello there!")

        fill(stackOf(Material.BLACK_STAINED_GLASS_PANE) {
            name("<gray>")
        })

        partition("0 0010101") {
            slot(stackOf(Material.DIAMOND) {
                name("<rainbow>the first one")
            })

            slot(stackOf(Material.DIRT) {
                name("<rainbow>the second one")
            })

            slot(stackOf(Material.COAL) {
                name("<rainbow>the third one")
            }) {
                schedule(SynchronizationContext.ASYNC) {

                }
            }
        }
    }

    override fun init() {
        registerCommands()
    }

    @CommandMethod("testmenus")
    fun testMenus(player: Player) {
        menu.open(player)
    }
}