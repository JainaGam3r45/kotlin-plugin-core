package com.jifercraft.core.module

interface PluginModule {
    fun onEnable()
    fun onDisable()

    // Default implementation so we don't have to override this everywhere
    fun onReload() {
        onDisable()
        onEnable()
    }
}
