package com.github.burrunan.multicache.internal

import com.github.burrunan.multicache.Multicache
import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory
import org.gradle.caching.configuration.BuildCache

open class MulticacheServiceFactory : BuildCacheServiceFactory<Multicache> {
    override fun createBuildCacheService(
        config: Multicache,
        describer: BuildCacheServiceFactory.Describer
    ): BuildCacheService {
        describer.type("multicache")
        val readCaches = mutableMapOf<BuildCacheDescriber, BuildCacheService>()
        val writeCaches = mutableMapOf<BuildCacheDescriber, BuildCacheService>()
        describer.config("read-order", config.selectedCaches.keys.toString())
        for ((name, cache) in config.selectedCaches) {
            val factoryType =
                config.buildCacheInternal.getBuildCacheServiceFactoryType(cache::class.java)

            @Suppress("UNCHECKED_CAST")
            val factory =
                config.instantiator.newInstance(factoryType) as BuildCacheServiceFactory<BuildCache>
            val subDescriber = BuildCacheDescriber()
            subDescriber.config("push", cache.isPush.toString())
            val buildCache = factory.createBuildCacheService(cache, subDescriber)
            describer.config(name, subDescriber.toString())
            readCaches[subDescriber] = buildCache
            if (cache.isPush) {
                writeCaches[subDescriber] = buildCache
            }
        }
        return MulticacheService(MulticacheRoutingConfig(readCaches, writeCaches))
    }
}
