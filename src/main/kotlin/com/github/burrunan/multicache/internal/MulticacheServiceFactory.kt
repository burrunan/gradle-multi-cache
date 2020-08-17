package com.github.burrunan.multicache.internal

import com.github.burrunan.multicache.Multicache
import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory
import org.gradle.caching.configuration.BuildCache
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

open class MulticacheServiceFactory: BuildCacheServiceFactory<Multicache> {
    override fun createBuildCacheService(
        config: Multicache,
        describer: BuildCacheServiceFactory.Describer
    ): BuildCacheService {
        describer.type("multicache")
        val downstream = config.caches.entries.associate { (name, cache) ->
            val factoryType =
                config.buildCacheInternal.getBuildCacheServiceFactoryType(cache::class.java)

            @Suppress("UNCHECKED_CAST")
            val factory =
                config.instantiator.newInstance(factoryType) as BuildCacheServiceFactory<BuildCache>
            val subDescriber = BuildCacheDescriber()
            val buildCache = factory.createBuildCacheService(cache, subDescriber)
            describer.config(name, subDescriber.toString())
            subDescriber to buildCache
        }
        return MulticacheService(MulticacheRoutingConfig(downstream))
    }
}
