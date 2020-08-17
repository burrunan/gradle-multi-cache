package com.github.burrunan.multicache.internal

import org.gradle.api.logging.Logging
import org.gradle.caching.BuildCacheEntryReader
import org.gradle.caching.BuildCacheEntryWriter
import org.gradle.caching.BuildCacheKey
import org.gradle.caching.BuildCacheService
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

private val logger = Logging.getLogger(MulticacheService::class.java)

open class MulticacheService(private val config: MulticacheRoutingConfig) : BuildCacheService {
    private val threadFactory = Executors.defaultThreadFactory()

    // WorkerExecutor is available in Project plugins only (as of 6.6) :(
    // https://docs.gradle.org/current/userguide/custom_gradle_types.html#service_injection
    private val executor = ThreadPoolExecutor(
        1,
        8,
        1,
        TimeUnit.MINUTES,
        SynchronousQueue(),
        { r -> threadFactory.newThread(r).apply { name = "multicache-$name" } }
    ) { r, pool -> pool.queue.put(r) }

    override fun close() {
        for (cache in config.caches.values) {
            cache.close()
        }
    }

    override fun load(key: BuildCacheKey, reader: BuildCacheEntryReader): Boolean {
        for ((name, cache) in config.caches) {
            logger.lifecycle("Fetching $key from $name")
            synchronized(key) {
                if (cache.load(key, reader)) {
                    if (logger.isDebugEnabled) {
                        logger.debug("Received cache entry $key from $name")
                    }
                    return true
                }
            }
        }
        return false
    }

    override fun store(key: BuildCacheKey, writer: BuildCacheEntryWriter) {
        val reusableWriter = writer.asReusable()
        val futures = config.caches.map { (name, cache) ->
            executor.submit {
                logger.lifecycle("Storing $key to $name")
                synchronized(name) {
                    cache.store(key, reusableWriter)
                }
            }
        }
        futures.forEach { it.get() }
    }
}
