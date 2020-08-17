package com.github.burrunan.multicache.internal

import org.gradle.caching.BuildCacheEntryWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

fun BuildCacheEntryWriter.file(): File? = try {
    this::class.java.getDeclaredField("file").run {
        isAccessible = true
        get(this@file) as? File
    }
} catch (ignore: Throwable) {
    null
}

fun BuildCacheEntryWriter.asReusable(): BuildCacheEntryWriter {
    val entrySize = size

    file()?.let { file ->
        return object : BuildCacheEntryWriter {
            override fun getSize() = entrySize

            override fun writeTo(output: OutputStream) {
                file.inputStream().use {
                    it.copyTo(output)
                }
            }
        }
    }

    return object : BuildCacheEntryWriter {
        override fun getSize() = entrySize

        private val data = ByteArrayOutputStream().let {
            writeTo(it)
            it.toByteArray()
        }

        override fun writeTo(output: OutputStream) {
            output.write(data)
        }
    }
}
