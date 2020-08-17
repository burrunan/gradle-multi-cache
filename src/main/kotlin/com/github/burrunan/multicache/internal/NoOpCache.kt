package com.github.burrunan.multicache.internal

import org.gradle.caching.*
import org.gradle.caching.configuration.AbstractBuildCache

open class NoOp : AbstractBuildCache()

open class NoOpCacheServiceFactory : BuildCacheServiceFactory<NoOp> {
    override fun createBuildCacheService(
        config: NoOp,
        describer: BuildCacheServiceFactory.Describer
    ) =
        NoOpCacheService()
}

open class NoOpCacheService : BuildCacheService {
    private fun notImplemented(): Nothing =
        TODO("Please call multicache.configure() to activate multi-cache")

    override fun close() = notImplemented()
    override fun load(key: BuildCacheKey, reader: BuildCacheEntryReader) = notImplemented()
    override fun store(key: BuildCacheKey, writer: BuildCacheEntryWriter) = notImplemented()
}
