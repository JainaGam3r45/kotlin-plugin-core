package com.jifercraft.core.logging

import com.jifercraft.core.CorePlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.logging.Level

class PluginLogger(private val plugin: CorePlugin) {

    private val prefix = Component.text("[${plugin.name}] ", NamedTextColor.GREEN)

    var debugEnabled: Boolean = false

    fun info(message: String) {
        plugin.server.consoleSender.sendMessage(prefixed(message, NamedTextColor.WHITE))
    }

    fun warn(message: String) {
        plugin.server.consoleSender.sendMessage(prefixed(message, NamedTextColor.YELLOW))
    }

    fun error(message: String, throwable: Throwable? = null) {
        plugin.server.consoleSender.sendMessage(prefixed(message, NamedTextColor.RED))
        if (throwable != null) {
            plugin.logger.log(Level.SEVERE, "Exception traceback:", throwable)
        }
    }

    fun debug(message: String) {
        if (!debugEnabled) return
        
        val debugPrefix = Component.text("[DEBUG] ", NamedTextColor.WHITE)
        plugin.server.consoleSender.sendMessage(
            prefix.append(debugPrefix).append(Component.text(message, NamedTextColor.WHITE))
        )
    }

    // Lazy eval for debug strings so we don't build strings if debug is off
    fun debug(message: () -> String) {
        if (debugEnabled) {
            debug(message())
        }
    }

    fun component(component: Component) {
        plugin.server.consoleSender.sendMessage(prefix.append(component))
    }

    private fun prefixed(message: String, color: NamedTextColor): Component {
        return prefix.append(Component.text(message, color))
    }
}
