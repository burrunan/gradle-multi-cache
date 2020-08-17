package com.github.burrunan.multicache

import com.github.burrunan.multicache.internal.NoOp
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.caching.configuration.BuildCache
import org.gradle.caching.configuration.BuildCacheConfiguration
import org.gradle.caching.configuration.internal.BuildCacheConfigurationInternal
import org.gradle.internal.reflect.Instantiator
import org.gradle.kotlin.dsl.remote
import javax.inject.Inject

open class MulticacheExtension @Inject constructor(
    private val buildCache: BuildCacheConfiguration,
    private val instantiator: Instantiator
) {
    private val downstreamCaches = mutableMapOf<String, BuildCache>()

    private val buildCacheInternal: BuildCacheConfigurationInternal =
        buildCache as BuildCacheConfigurationInternal

    @Suppress("MemberVisibilityCanBePrivate")
    fun push(name: String) {
        if (name in downstreamCaches) {
            throw GradleException("Cache $name has already been registered for multi-cache. Please use different name")
        }
        downstreamCaches[name] = buildCache.remote ?: return
        // Reset build cache configuration to avoid producing warnings
        // buildCacheInternal.remote = null
        buildCache.remote<NoOp>()
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun pushAndConfigure(name: String, action: Action<Multicache>) {
        push(name)
        configure(action)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun configure(action: Action<Multicache>) {
        if (downstreamCaches.isEmpty()) {
            println("No build caches were added via multicache.push(String)")
            return
        }
        buildCache.remote<Multicache> {
            buildCacheInternal = this@MulticacheExtension.buildCacheInternal
            instantiator = this@MulticacheExtension.instantiator
            caches += downstreamCaches
            action.execute(this)
        }
    }
}
