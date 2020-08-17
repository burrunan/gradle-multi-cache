package com.github.burrunan.multicache.internal

import com.github.burrunan.multicache.Multicache
import org.gradle.api.logging.Logging
import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory
import org.gradle.caching.configuration.BuildCache

private val logger = Logging.getLogger(MulticacheServiceFactory::class.java)

open class MulticacheServiceFactory : BuildCacheServiceFactory<Multicache> {
    override fun createBuildCacheService(
        config: Multicache,
        describer: BuildCacheServiceFactory.Describer
    ): BuildCacheService {
        describer.type("multicache")
        val readCaches = mutableMapOf<BuildCacheDescriber, BuildCacheService>()
        val writeCaches = mutableMapOf<BuildCacheDescriber, BuildCacheService>()
        for ((name, cache) in config.selectedCaches) {
            if (!cache.isEnabled) {
                logger.info("Cache $name is disabled")
                continue
            }
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
