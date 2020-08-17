package com.github.burrunan.multicache

import org.gradle.api.logging.Logging
import org.gradle.caching.configuration.AbstractBuildCache
import org.gradle.caching.configuration.BuildCache
import org.gradle.caching.configuration.internal.BuildCacheConfigurationInternal
import org.gradle.internal.reflect.Instantiator

private val logger = Logging.getLogger(Multicache::class.java)

open class Multicache : AbstractBuildCache() {
    internal val caches = mutableMapOf<String, BuildCache>()
    internal val selectedCaches = mutableMapOf<String, BuildCache>()

    internal lateinit var instantiator: Instantiator
    internal lateinit var buildCacheInternal: BuildCacheConfigurationInternal

    @Suppress("unused")
    fun loadSequentiallyWriteConcurrently(vararg cacheNames: String) {
        for (name in cacheNames) {
            val cache = caches.getValue(name)
            if (!cache.isEnabled) {
                logger.info("Cache {} is disabled", name)
                continue
            }
            selectedCaches[name] = cache
            isPush = isPush or cache.isPush
        }
    }
}
