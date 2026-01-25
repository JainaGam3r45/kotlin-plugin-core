package com.jifercraft.core.config

import com.jifercraft.core.CorePlugin
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class PluginConfig(private val plugin: CorePlugin) {

    private lateinit var root: YamlConfiguration
    private val configs = mutableMapOf<String, YamlConfiguration>()

    val file: File get() = File(plugin.dataFolder, "config.yml")

    fun load() {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()
        root = plugin.config as YamlConfiguration
    }

    fun section(path: String): ConfigurationSection? {
        return root.getConfigurationSection(path)
    }

    // Still getting used to operator overloading but this is pretty cool
    operator fun <T> get(path: String, default: T): T {
        @Suppress("UNCHECKED_CAST")
        return (root.get(path) as? T) ?: default
    }

    fun loadFile(name: String): YamlConfiguration {
        return configs.getOrPut(name) {
            val target = File(plugin.dataFolder, name)
            if (!target.exists()) {
                plugin.saveResource(name, false)
            }
            YamlConfiguration.loadConfiguration(target)
        }
    }

    fun reload(name: String? = null) {
        if (name != null) {
            configs.remove(name)
            loadFile(name)
        } else {
            load()
            val keys = configs.keys.toList()
            configs.clear()
            for (key in keys) {
                loadFile(key)
            }
        }
    }

    fun save(name: String) {
        configs[name]?.let { cfg ->
            cfg.save(File(plugin.dataFolder, name))
        }
    }
}
