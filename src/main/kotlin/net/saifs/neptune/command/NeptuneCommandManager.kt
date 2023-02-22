package net.saifs.neptune.command

import cloud.commandframework.CommandManager
import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.exceptions.NoPermissionException
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.saifs.neptune.NeptunePlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_19_R2.CraftServer
import java.util.function.Function

class NeptuneCommandManager(plugin: NeptunePlugin) {
    companion object {
        private val NO_PERMISSION_FUNCTION: (Exception) -> Component = {
            MiniMessage.miniMessage()
                .deserialize(
                    "<#ff0000>You do not have permission to do that! " +
                            "You are missing the permission node: <#8ec3cf>" + (it as NoPermissionException).missingPermission + "<#ff0000>."
                )
        }
    }

    private var commandManager: PaperCommandManager<CommandSender> = PaperCommandManager(
        plugin,
        AsynchronousCommandExecutionCoordinator.simpleCoordinator(),
        Function.identity(),
        Function.identity()
    )
    private val commandMetaFunction: (ParserParameters) -> CommandMeta = {
        CommandMeta.simple().with(CommandMeta.DESCRIPTION, it.get(StandardParameters.DESCRIPTION, "No Description"))
            .build()
    }
    private val annotationParser: AnnotationParser<CommandSender> =
        AnnotationParser(commandManager, CommandSender::class.java, commandMetaFunction)

    init {
        commandManager.registerBrigadier()

        commandManager.setSetting(CommandManager.ManagerSettings.ALLOW_UNSAFE_REGISTRATION, true)
        commandManager.setSetting(CommandManager.ManagerSettings.OVERRIDE_EXISTING_COMMANDS, true)

        MinecraftExceptionHandler<CommandSender>()
            .withInvalidSenderHandler()
            .withInvalidSyntaxHandler()
            .withArgumentParsingHandler()
            .withCommandExecutionHandler()
            .withHandler(MinecraftExceptionHandler.ExceptionType.NO_PERMISSION, NO_PERMISSION_FUNCTION)
            .apply(commandManager) { it }
    }

    fun <T : Any> registerCommands(clazz: Class<T>) {
        val constructor = clazz.getDeclaredConstructor()
        val instance = constructor.newInstance()
        annotationParser.parse(instance)
    }

    fun close() {
        for (cmd in commandManager.rootCommands()) {
            commandManager.deleteRootCommand(cmd)
        }

        syncCommands()
    }

    fun syncCommands() {
        (Bukkit.getServer() as CraftServer).syncCommands()
    }

    internal fun registerHelp(root: String, vararg aliases: String) {
        val help = MinecraftHelp("/$root help", { it }, commandManager)
        help.helpColors = MinecraftHelp.HelpColors.of(
            TextColor.color(240, 81, 226), // Primary
            TextColor.color(9, 147, 232), // Highlight
            TextColor.color(86, 198, 232), //alternateHighlight
            TextColor.color(142, 195, 207),
            TextColor.color(73, 252, 255) // accent
        )
        help.setMaxResultsPerPage(15)
        commandManager.command(
            commandManager.commandBuilder(root, *aliases)
                .literal("help")
                .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
                .handler {
                    val query = it.getOrDefault("query", "")
                    help.queryCommands(query, it.sender)
                }.build()
        )
        syncCommands()
    }
}