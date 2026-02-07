package com.jifercraft.core.command

import com.jifercraft.core.CorePlugin
import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

@Suppress("UnstableApiUsage")
object CoreCommand {

    fun register(plugin: CorePlugin) {
        CommandRegistry.register("core", "Manage the core plugin", listOf("kpc")) {
            requires { it.sender.hasPermission("core.admin") }

            then(
                Commands.literal("reload")
                    .executes { ctx ->
                        plugin.reload()
                        ctx.source.sender.sendMessage(
                            Component.text("Config & modules reloaded.", NamedTextColor.GREEN)
                        )
                        Command.SINGLE_SUCCESS
                    }
            )

            then(
                Commands.literal("modules")
                    .executes { ctx ->
                        val sender = ctx.source.sender
                        val msg = Component.text("Loaded modules: ", NamedTextColor.GOLD)
                            .append(Component.text("${plugin.modules.enabledCount} active", NamedTextColor.GREEN))
                        
                        sender.sendMessage(msg)
                        Command.SINGLE_SUCCESS
                    }
            )

            then(
                Commands.literal("debug")
                    .executes { ctx ->
                        plugin.log.debugEnabled = !plugin.log.debugEnabled
                        val state = if (plugin.log.debugEnabled) "enabled" else "disabled"
                        ctx.source.sender.sendMessage(
                            Component.text("Debug mode is now $state.", NamedTextColor.AQUA)
                        )
                        Command.SINGLE_SUCCESS
                    }
            )

            // The base /core command
            executes { ctx ->
                val sender = ctx.source.sender
                sender.sendMessage(
                    Component.text("KotlinPluginCore ", NamedTextColor.GOLD)
                        .append(Component.text("v${plugin.pluginMeta.version}", NamedTextColor.WHITE))
                )
                Command.SINGLE_SUCCESS
            }
        }
    }
}
