package com.jifercraft.core.module

import com.jifercraft.core.CorePlugin

class ModuleRegistry(private val plugin: CorePlugin) {

    @PublishedApi
    internal val modules = linkedMapOf<String, RegisteredModule>()

    val enabledCount: Int 
        get() = modules.values.count { it.enabled }

    fun register(module: PluginModule) {
        val info = module::class.java.getAnnotation(ModuleInfo::class.java)
        
        if (info == null) {
            plugin.log.error("can't register ${module::class.simpleName} - missing @ModuleInfo")
            return
        }

        if (modules.containsKey(info.id)) {
            plugin.log.warn("module '${info.id}' is already registered, skipping")
            return
        }

        modules[info.id] = RegisteredModule(info, module)
        plugin.log.debug("registered module: ${info.id}")
    }

    fun discoverAndEnable() {
        for (entry in modules.values) {
            enableModule(entry)
        }
    }

    fun disableAll() {
        // Reverse order so dependencies tear down correctly
        val active = modules.values.filter { it.enabled }.reversed()
        for (entry in active) {
            disableModule(entry)
        }
    }

    fun reloadAll() {
        modules.values.forEach { entry ->
            try {
                entry.module.onReload()
                entry.enabled = true
                plugin.log.debug("reloaded ${entry.info.id}")
            } catch (e: Exception) {
                plugin.log.error("failed to reload module ${entry.info.id}", e)
                entry.enabled = false
            }
        }
    }

    fun get(id: String): PluginModule? {
        return modules[id]?.module
    }

    inline fun <reified T : PluginModule> get(): T? {
        return modules.values.find { it.module is T }?.module as? T
    }

    private fun enableModule(entry: RegisteredModule) {
        try {
            entry.module.onEnable()
            entry.enabled = true
            plugin.log.debug("enabled ${entry.info.id}")
        } catch (e: Exception) {
            plugin.log.error("failed to enable module ${entry.info.id}", e)
            entry.enabled = false
        }
    }

    private fun disableModule(entry: RegisteredModule) {
        try {
            entry.module.onDisable()
            entry.enabled = false
            plugin.log.debug("disabled ${entry.info.id}")
        } catch (e: Exception) {
            plugin.log.error("failed to disable module ${entry.info.id}", e)
        }
    }

    // @PublishedApi so the inline reified get() can access it
    @PublishedApi
    internal data class RegisteredModule(
        val info: ModuleInfo,
        val module: PluginModule,
        var enabled: Boolean = false
    )
}
