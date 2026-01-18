package com.jifercraft.core.module

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModuleInfo(
    val id: String,
    val description: String = ""
)
