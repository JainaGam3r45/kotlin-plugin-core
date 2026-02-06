package com.jifercraft.core.command

import com.jifercraft.core.CorePlugin
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents

// Paper recommends using Brigadier now. I'm storing them and flushing during the lifecycle event.
@Suppress("UnstableApiUsage")
object CommandRegistry {

    private val pending = mutableListOf<PendingCommand>()
    private var initialized = false

    fun init(plugin: CorePlugin) {
        if (initialized) return
        initialized = true

        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()
            
            for (cmd in pending) {
                registrar.register(cmd.node.build(), cmd.description, cmd.aliases)
            }
            
            plugin.log.debug("registered ${pending.size} command(s)")
            pending.clear()
        }
    }

    fun register(
        name: String,
        description: String = "",
        aliases: List<String> = emptyList(),
        builder: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit
    ) {
        val node = Commands.literal(name)
        node.apply(builder) // using apply to configure the node block
        pending.add(PendingCommand(node, description, aliases))
    }

    private data class PendingCommand(
        val node: LiteralArgumentBuilder<CommandSourceStack>,
        val description: String,
        val aliases: List<String>
    )
}
