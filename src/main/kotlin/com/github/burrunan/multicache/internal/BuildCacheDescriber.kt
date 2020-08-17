package com.github.burrunan.multicache.internal

import org.gradle.caching.BuildCacheServiceFactory

class BuildCacheDescriber : BuildCacheServiceFactory.Describer {
    var type: String = ""
    var params = mutableMapOf<String, String>()

    override fun type(type: String): BuildCacheServiceFactory.Describer = apply {
        this.type = type
    }

    override fun config(name: String, value: String): BuildCacheServiceFactory.Describer = apply {
        params[name] = value
    }

    private val stringValue by lazy { "$type $params" }

    override fun toString() = stringValue
}
