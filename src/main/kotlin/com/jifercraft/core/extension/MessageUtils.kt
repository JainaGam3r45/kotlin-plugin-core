package com.jifercraft.core.extension

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

// I know Paper wants everyone using MiniMessage tags now,
// but users are 100% going to paste ampersands and hex codes from their old 
// Spigot configs anyway. Easier to just blindly translate them before parsing.
object MessageUtils {

    private val mm = MiniMessage.miniMessage()
    private val hexRegex = Regex("&#([A-Fa-f0-9]{6})|#([A-Fa-f0-9]{6})")

    private val legacyMap = mapOf(
        "&0" to "<black>", "&1" to "<dark_blue>", "&2" to "<dark_green>", "&3" to "<dark_aqua>",
        "&4" to "<dark_red>", "&5" to "<dark_purple>", "&6" to "<gold>", "&7" to "<gray>",
        "&8" to "<dark_gray>", "&9" to "<blue>", "&a" to "<green>", "&b" to "<aqua>",
        "&c" to "<red>", "&d" to "<light_purple>", "&e" to "<yellow>", "&f" to "<white>",
        "&k" to "<obfuscated>", "&l" to "<bold>", "&m" to "<strikethrough>", "&n" to "<underlined>",
        "&o" to "<italic>", "&r" to "<reset>"
    )

    fun parse(input: String): Component {
        var text = input

        text = text.replace(hexRegex) {
            val hex = it.groupValues[1].takeIf { g -> g.isNotEmpty() } ?: it.groupValues[2]
            "<#$hex>"
        }

        for ((legacy, miniMessageTag) in legacyMap) {
            text = text.replace(legacy, miniMessageTag)
        }

        return mm.deserialize(text)
    }
}
