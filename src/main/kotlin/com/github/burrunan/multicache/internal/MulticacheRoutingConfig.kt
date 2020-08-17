package com.github.burrunan.multicache.internal

import org.gradle.caching.BuildCacheService

class MulticacheRoutingConfig(
    val readCaches: Map<BuildCacheDescriber, BuildCacheService>,
    val writeCaches: Map<BuildCacheDescriber, BuildCacheService>
)
