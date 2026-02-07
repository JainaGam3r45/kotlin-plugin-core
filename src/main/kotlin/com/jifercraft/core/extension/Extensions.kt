package com.jifercraft.core.extension

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

private val miniMessage = MiniMessage.miniMessage()

// Extension functions, still one of my favorite Kotlin features coming from Java
fun String.parseMM(): Component {
    return miniMessage.deserialize(this)
}

fun Player.sendMM(message: String) {
    this.sendMessage(message.parseMM())
}

fun Player.sendError(message: String) {
    this.sendMessage(Component.text(message, NamedTextColor.RED))
}

fun Player.sendSuccess(message: String) {
    this.sendMessage(Component.text(message, NamedTextColor.GREEN))
}

fun JavaPlugin.registerListener(listener: Listener) {
    this.server.pluginManager.registerEvents(listener, this)
}
