package com.jifercraft.core

import com.jifercraft.core.command.CommandRegistry
import com.jifercraft.core.command.CoreCommand
import com.jifercraft.core.config.PluginConfig
import com.jifercraft.core.logging.PluginLogger
import com.jifercraft.core.module.ModuleRegistry
import com.jifercraft.core.module.example.ExampleModule
import com.jifercraft.core.service.ServiceRegistry
import org.bukkit.plugin.java.JavaPlugin

class CorePlugin : JavaPlugin() {

    lateinit var services: ServiceRegistry
    lateinit var modules: ModuleRegistry
    lateinit var pluginConfig: PluginConfig
    lateinit var log: PluginLogger

    override fun onEnable() {
        instance = this

        log = PluginLogger(this)
        services = ServiceRegistry()
        pluginConfig = PluginConfig(this)
        modules = ModuleRegistry(this)

        // Make these available so modules don't have to pass them around manually
        services.register(log)
        services.register(pluginConfig)

        CommandRegistry.init(this)
        pluginConfig.load()

        log.debugEnabled = pluginConfig["debug", false]

        CoreCommand.register(this)
        
        // Testing module, ditch this when making a real plugin
        modules.register(ExampleModule())
        modules.discoverAndEnable()

        log.info("enabled (${modules.enabledCount} modules loaded)")
    }

    override fun onDisable() {
        modules.disableAll()
        services.clear()
        log.info("disabled")
    }

    fun reload() {
        log.info("reloading...")
        modules.disableAll()
        pluginConfig.load()
        log.debugEnabled = pluginConfig["debug", false]
        modules.reloadAll()
        log.info("reload complete, ${modules.enabledCount} modules active")
    }

    companion object {
        // Singleton pattern from Java but looking cleaner in Kotlin
        lateinit var instance: CorePlugin
            private set
    }
}
