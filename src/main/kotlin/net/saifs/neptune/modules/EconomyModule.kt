package net.saifs.neptune.modules

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import net.kyori.adventure.text.Component
import net.saifs.neptune.core.modules.ModuleData
import net.saifs.neptune.core.modules.NeptuneModule
import net.saifs.neptune.util.DOUBLE_RIGHT_ARROW
import net.saifs.neptune.util.formatNumber
import net.saifs.neptune.util.parseMini
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@ModuleData("economy")
class EconomyModule : NeptuneModule() {
    private lateinit var currency: Currency

    override fun init() {
        currency = modules.get<CurrenciesModule>().newCurrency("money")
        registerCommands().registerHelp("economy", "eco")
    }

    @CommandMethod("balance|bal <player>")
    @CommandDescription("Views yours or other players balance")
    fun balanceCommand(sender: CommandSender, @Argument("player") player: Player) {
        val balance = currency[player]
        sender.sendMessage(parseMini("<#68ed90><bold>ECONOMY</bold> <gray>$DOUBLE_RIGHT_ARROW</gray> $<balance>", mutableMapOf(
           Pair("balance", Component.text(formatNumber(balance)))
        )))
    }

    @CommandMethod("economy|eco give <player> <amount>")
    @CommandPermission("economy.admin")
    fun giveCommand(sender: CommandSender, @Argument("player") player: Player, @Argument("amount") amount: Float) {
        currency[player] = currency[player] + amount
        sender.sendMessage(parseMini("<#68ed90><bold>ECONOMY</bold> <gray>$DOUBLE_RIGHT_ARROW</gray> <player>'s new balance is $<balance>", mutableMapOf(
            Pair("balance", Component.text(formatNumber(currency[player]))),
            Pair("player", Component.text(player.name))
        )))
    }

    @CommandMethod("economy|eco set <player> <amount>")
    @CommandPermission("economy.admin")
    fun setCommand(sender: CommandSender, @Argument("player") player: Player, @Argument("amount") amount: Float) {
        currency[player] = amount
        sender.sendMessage(parseMini("<#68ed90><bold>ECONOMY</bold> <gray>$DOUBLE_RIGHT_ARROW</gray> <player>'s new balance is $<balance>", mutableMapOf(
            Pair("balance", Component.text(formatNumber(amount))),
            Pair("player", Component.text(player.name))
        )))
    }
}