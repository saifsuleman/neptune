package net.saifs.neptune.modulestest

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import net.kyori.adventure.text.Component
import net.saifs.neptune.modules.ModuleData
import net.saifs.neptune.modules.NeptuneModule
import net.saifs.neptune.scheduling.SynchronizationContext
import net.saifs.neptune.scheduling.schedule
import net.saifs.neptune.util.DOUBLE_RIGHT_ARROW
import net.saifs.neptune.util.formatNumber
import net.saifs.neptune.util.parseMini
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@ModuleData("economy")
class EconomyModule : NeptuneModule() {
    private lateinit var currency: Currency

    override fun init() {
        currency = modules.get<CurrenciesModule>().newCurrency("money")
        registerCommands().registerHelp("economy", "eco")
    }

    @CommandMethod("baltop [page]")
    @CommandDescription("Views the baltop")
    fun baltopCommand(sender: CommandSender, @Argument("page", defaultValue = "1") page: Int) {
        schedule(SynchronizationContext.ASYNC) {
            val baltop = currency.getLeaderboard((page - 1) * 15, page * 15)
            if (baltop.isEmpty()) {
                sender.sendMessage(parseMini("<#68ed90><bold>ECONOMY</bold> <gray>$DOUBLE_RIGHT_ARROW</gray><red> There are no entries on the baltop!"))
                return@schedule
            }

            val components = mutableListOf(parseMini("<#68ed90><bold>BALANCE TOP:"))
            for ((i, pair) in baltop.withIndex()) {
                val (uuid, balance) = pair
                val playerName = Bukkit.getOfflinePlayer(uuid).name ?: "null"
                components.add(parseMini("<gray><num>.</gray> <#68ed90><player><gray>:</gray> <balance>", mutableMapOf(
                    Pair("num", Component.text(formatNumber(i + 1 + ((page-1) * 15)))),
                    Pair("player", Component.text(playerName)),
                    Pair("balance", Component.text("$${formatNumber(balance)}"))
                )))
            }

            components.forEach(sender::sendMessage)
        }
    }

    @CommandMethod("balance|bal <player>")
    @CommandDescription("Views other players balance")
    fun balanceOtherCommand(sender: CommandSender, @Argument("player") player: Player) {
        val balance = currency[player]
        sender.sendMessage(parseMini("<#68ed90><bold>ECONOMY</bold> <gray>$DOUBLE_RIGHT_ARROW</gray> <player>'s balance is $<balance>", mutableMapOf(
            Pair("balance", Component.text(formatNumber(balance))),
            Pair("player", player.displayName())
        )))
    }

    @CommandMethod("balance|bal")
    @CommandDescription("Views your balance")
    fun balanceCommand(sender: Player) {
        val balance = currency[sender]
        sender.sendMessage(parseMini("<#68ed90><bold>ECONOMY</bold> <gray>$DOUBLE_RIGHT_ARROW</gray> $<balance>", mutableMapOf(
            Pair("balance", Component.text(formatNumber(balance))),
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