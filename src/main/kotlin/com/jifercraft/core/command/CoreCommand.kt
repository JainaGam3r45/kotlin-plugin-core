package com.jifercraft.core.command

import com.jifercraft.core.CorePlugin
import com.jifercraft.core.extension.MessageUtils
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage

@Suppress("UnstableApiUsage")
object CoreCommand {

    fun register(plugin: CorePlugin) {
        CommandRegistry.register("core", "Manage the core plugin", listOf("kpc")) {

            then(Commands.literal("reload").executes { ctx -> handleReload(ctx, plugin) })
            then(Commands.literal("help").executes { ctx -> handleHelp(ctx, plugin) })
            then(Commands.literal("modules").executes { ctx -> handleModules(ctx, plugin) })
            then(Commands.literal("debug").executes { ctx -> handleDebug(ctx, plugin) })

            then(
                Commands.argument("fallback", StringArgumentType.greedyString())
                    .executes { ctx -> handleUnknown(ctx, plugin) }
            )

            executes { ctx -> handleAbout(ctx, plugin) }
        }
    }

    private fun handleReload(ctx: CommandContext<CommandSourceStack>, plugin: CorePlugin): Int {
        val sender = ctx.source.sender
        if (!sender.hasPermission("core.reload")) {
            sender.sendMessage(plugin.pluginConfig.messages.noPermission)
            return Command.SINGLE_SUCCESS
        }

        plugin.reload()
        sender.sendMessage(plugin.pluginConfig.messages.reloadSuccess)
        return Command.SINGLE_SUCCESS
    }

    private fun handleHelp(ctx: CommandContext<CommandSourceStack>, plugin: CorePlugin): Int {
        val sender = ctx.source.sender
        if (!sender.hasPermission("core.help")) {
            sender.sendMessage(plugin.pluginConfig.messages.noPermission)
            return Command.SINGLE_SUCCESS
        }

        val mm = MiniMessage.miniMessage()
        sender.sendMessage(mm.deserialize("<gold>Core Commands:</gold>"))
        sender.sendMessage(mm.deserialize("<gold>/core</gold> <white>- Shows plugin info</white>"))
        sender.sendMessage(mm.deserialize("<gold>/core help</gold> <white>- Shows this list of commands</white>"))

        if (sender.hasPermission("core.reload")) {
            sender.sendMessage(mm.deserialize("<gold>/core reload</gold> <white>- Reloads plugin configuration and modules</white>"))
        }
        if (sender.hasPermission("core.debug")) {
            sender.sendMessage(mm.deserialize("<gold>/core modules</gold> <white>- Shows active modules</white>"))
            sender.sendMessage(mm.deserialize("<gold>/core debug</gold> <white>- Toggles debug logging</white>"))
        }

        return Command.SINGLE_SUCCESS
    }

    private fun handleModules(ctx: CommandContext<CommandSourceStack>, plugin: CorePlugin): Int {
        val sender = ctx.source.sender
        if (!sender.hasPermission("core.debug")) {
            sender.sendMessage(plugin.pluginConfig.messages.noPermission)
            return Command.SINGLE_SUCCESS
        }

        val raw = plugin.pluginConfig.messages.modulesListRaw
            .replace("{count}", plugin.modules.enabledCount.toString())

        sender.sendMessage(MessageUtils.parse(raw))
        return Command.SINGLE_SUCCESS
    }

    private fun handleDebug(ctx: CommandContext<CommandSourceStack>, plugin: CorePlugin): Int {
        val sender = ctx.source.sender
        if (!sender.hasPermission("core.debug")) {
            sender.sendMessage(plugin.pluginConfig.messages.noPermission)
            return Command.SINGLE_SUCCESS
        }

        plugin.log.debugEnabled = !plugin.log.debugEnabled
        val state = if (plugin.log.debugEnabled) "enabled" else "disabled"

        val raw = plugin.pluginConfig.messages.debugToggleRaw
            .replace("{state}", state)

        sender.sendMessage(MessageUtils.parse(raw))
        return Command.SINGLE_SUCCESS
    }

    private fun handleUnknown(ctx: CommandContext<CommandSourceStack>, plugin: CorePlugin): Int {
        ctx.source.sender.sendMessage(plugin.pluginConfig.messages.unknownSubcommand)
        return Command.SINGLE_SUCCESS
    }

    private fun handleAbout(ctx: CommandContext<CommandSourceStack>, plugin: CorePlugin): Int {
        val sender = ctx.source.sender
        val meta = plugin.pluginMeta

        // Still love the elvis operator for this stuff
        val desc = meta.description ?: "A core plugin."
        val authors = meta.authors.joinToString(", ")

        val mm = MiniMessage.miniMessage()
        sender.sendMessage(mm.deserialize("<gold>${meta.name}</gold> <white>v${meta.version}</white>"))
        if (authors.isNotEmpty()) {
            sender.sendMessage(mm.deserialize("<gold>Author:</gold> <white>$authors</white>"))
        }
        sender.sendMessage(mm.deserialize("<white>$desc</white>"))

        return Command.SINGLE_SUCCESS
    }
}
