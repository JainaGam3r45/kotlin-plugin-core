package com.jifercraft.core.module.example

import com.jifercraft.core.CorePlugin
import com.jifercraft.core.command.CommandRegistry
import com.jifercraft.core.extension.registerListener
import com.jifercraft.core.extension.sendMM
import com.jifercraft.core.module.ModuleInfo
import com.jifercraft.core.module.PluginModule
import com.mojang.brigadier.Command
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

@ModuleInfo(id = "example", description = "Just testing the module system")
class ExampleModule : PluginModule, Listener {

    private val plugin = CorePlugin.instance

    @Suppress("UnstableApiUsage")
    override fun onEnable() {
        plugin.registerListener(this)

        CommandRegistry.register("hello", "Say hello") {
            executes { ctx ->
                ctx.source.sender.sendPlainMessage("Hey, this module system actually works!")
                Command.SINGLE_SUCCESS
            }
        }

        plugin.log.info("example module loaded")
    }

    override fun onDisable() {
        // Important: unregister listeners so they don't double up on reload
        PlayerJoinEvent.getHandlerList().unregister(this)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val configMsg = plugin.pluginConfig["modules.example.welcome-message", "<green>Welcome <player>!</green>"]
        
        // Let's use the extension function for minimessage
        val finalMsg = configMsg.replace("<player>", event.player.name)
        event.player.sendMM(finalMsg)
    }
}
