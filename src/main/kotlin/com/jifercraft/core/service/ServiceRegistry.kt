package com.jifercraft.core.service

class ServiceRegistry {

    // Using a map to share dependencies between modules instead of passing everything in constructors
    @PublishedApi
    internal val services = mutableMapOf<Class<*>, Any>()

    inline fun <reified T : Any> register(service: T) {
        register(T::class.java, service)
    }

    fun <T : Any> register(type: Class<T>, service: T) {
        services[type] = service
    }

    inline fun <reified T : Any> get(): T {
        return get(T::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(type: Class<T>): T {
        val service = services[type]
            ?: throw IllegalStateException("Tried to get ${type.simpleName} but it wasn't registered!")
            
        return service as T
    }

    inline fun <reified T : Any> getOrNull(): T? {
        return services[T::class.java] as? T
    }

    fun clear() {
        services.clear()
    }
}
