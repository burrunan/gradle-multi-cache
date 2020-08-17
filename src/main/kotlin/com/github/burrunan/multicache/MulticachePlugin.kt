package com.github.burrunan.multicache

import com.github.burrunan.multicache.internal.MulticacheServiceFactory
import com.github.burrunan.multicache.internal.NoOp
import com.github.burrunan.multicache.internal.NoOpCacheServiceFactory
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.PluginManager
import org.gradle.internal.reflect.Instantiator
import org.gradle.kotlin.dsl.create

@Suppress("unused")
class MulticachePlugin : Plugin<Settings> {
    override fun apply(target: Settings) = target.run {
        buildCache.registerBuildCacheService(
            Multicache::class.java,
            MulticacheServiceFactory::class.java
        )
        buildCache.registerBuildCacheService(
            NoOp::class.java,
            NoOpCacheServiceFactory::class.java
        )
        extensions.create<MulticacheExtension>(
            "multicache",
            buildCache,
            findInstantiator(pluginManager)
        )
        Unit
    }

    private fun findInstantiator(pluginManager: PluginManager): Instantiator {
        var klass: Class<*> = pluginManager::class.java
        while (klass != Object::class.java) {
            try {
                return klass.getDeclaredField("instantiator").run {
                    isAccessible = true
                    get(pluginManager) as Instantiator
                }
            } catch (ignore: NoSuchFieldException) {
            }
            try {
                return klass.getDeclaredMethod("instantiatePlugin", Class::class.java).run {
                    isAccessible = true
                    invoke(pluginManager, Instantiator::class.java) as Instantiator
                }
            } catch (ignore: NoSuchMethodException) {
            }
            klass = klass.superclass ?: break
        }
        throw GradleException("Unable to get Instantiator instance")
    }
}
