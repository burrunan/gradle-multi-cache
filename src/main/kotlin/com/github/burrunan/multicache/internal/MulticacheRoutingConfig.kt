package com.github.burrunan.multicache.internal

import org.gradle.caching.BuildCacheService

class MulticacheRoutingConfig(
    val caches: Map<BuildCacheDescriber, BuildCacheService>
)
