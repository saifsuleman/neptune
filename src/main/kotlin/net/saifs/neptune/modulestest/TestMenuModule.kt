package net.saifs.neptune.modulestest

import cloud.commandframework.annotations.CommandMethod
import net.saifs.neptune.extensions.stackOf
import net.saifs.neptune.modules.ModuleData
import net.saifs.neptune.modules.NeptuneModule
import net.saifs.neptune.util.sendMini
import org.bukkit.Material
import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ModuleData("test-menus")
class TestMenuModule : NeptuneModule() {
    private val menu = menus.newMenu {
        size(27)
        fill(stackOf(Material.BLACK_STAINED_GLASS_PANE))
        slot(1, 2, stackOf(Material.DIAMOND)) {
            it.player.sendMessage("you just clicked!!!")
        }
    }

    override fun init() {
        config<TestConfig>()
        registerCommands()
    }

    @CommandMethod("testmenus")
    fun testMenus(player: Player) {
        menu.open(player)
    }

    @CommandMethod("testing")
    fun testCommand(player: Player) {
        player.sendMini(config<TestConfig>().msg)
    }
}

@ConfigSerializable
class TestConfig {
    var msg: String = "<rainbow>example test"
}