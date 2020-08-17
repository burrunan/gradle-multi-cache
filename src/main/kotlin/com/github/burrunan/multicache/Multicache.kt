package com.github.burrunan.multicache

import org.gradle.caching.configuration.AbstractBuildCache
import org.gradle.caching.configuration.BuildCache
import org.gradle.caching.configuration.internal.BuildCacheConfigurationInternal
import org.gradle.internal.reflect.Instantiator

open class Multicache : AbstractBuildCache() {
    internal val caches = mutableMapOf<String, BuildCache>()
    private val selectedCaches = mutableMapOf<String, BuildCache>()

    internal lateinit var instantiator: Instantiator
    internal lateinit var buildCacheInternal: BuildCacheConfigurationInternal

    @Suppress("unused")
    fun loadSequentiallyWriteConcurrently(vararg cacheNames: String) {
        for (name in cacheNames) {
            val cache = caches.getValue(name)
            selectedCaches[name] = cache
            isPush = isPush or cache.isPush
        }
    }
}
