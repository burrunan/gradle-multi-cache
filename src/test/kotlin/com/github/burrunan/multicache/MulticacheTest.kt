package com.github.burrunan.multicache

import org.gradle.api.JavaVersion
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class MulticacheTest : BaseGradleTest() {
    companion object {
        val isCI = System.getenv().containsKey("CI") || System.getProperties().containsKey("CI")

        @JvmStatic
        private fun gradleVersionAndSettings(): Iterable<Arguments> {
            if (!isCI) {
                // Use the minimal supported version when running locally
                // This makes tests faster when running locally
                return listOf(Arguments.arguments("5.0"))
            }
            return mutableListOf<Arguments>().apply {
                if (JavaVersion.current() <= JavaVersion.VERSION_12) {
                    addAll(
                            listOf(
                                    Arguments.arguments("5.6.2"),
                                    Arguments.arguments("5.4.1")
                            )
                    )
                }
                add(Arguments.arguments("6.0"))
                add(Arguments.arguments("6.6"))
            }
        }
    }

    @BeforeEach
    fun setup() {
        projectDir.resolve("gradle.properties").write(
                """
            org.gradle.caching=true
            org.gradle.caching.debug=true
        """.trimIndent()
        )
    }

    @ParameterizedTest
    @MethodSource("gradleVersionAndSettings")
    fun basic(gradleVersion: String) {
        val cacheA = projectDir.resolve("cacheA")
        createSettings(
                """
            buildCache {
                local {
                    // Only remote cache should be used
                    enabled = false
                }
                remote(org.gradle.caching.local.DirectoryBuildCache) {
                    push = true
                    directory = '$cacheA'
                }
                multicache.push("cacheA")
                remote(org.gradle.caching.local.DirectoryBuildCache) {
                    push = true
                    directory = '${projectDir.resolve("cacheB")}'
                }
                multicache.pushAndConfigure("cacheB") {
                    loadSequentiallyWriteConcurrently('cacheA', 'cacheB')
                }
            }
        """.trimIndent()
        )

        val outputFile = "build/out.txt"
        projectDir.resolve("build.gradle").write(
                """
            tasks.create('props', WriteProperties) {
              outputFile = file("$outputFile")
              property("hello", "world")
            }
            tasks.create('props2', WriteProperties) {
              outputFile = file("${outputFile}2")
              property("hello", "world2")
            }
        """.trimIndent()
        )
        val result = prepare(gradleVersion, "props", "-i", "-s").build()
        if (isCI) {
            println(result.output)
        }
        Assertions.assertEquals(TaskOutcome.SUCCESS, result.task(":props")?.outcome) {
            "first execution => no cache available,"
        }
        // Delete output to force task re-execution
        Assertions.assertTrue(cacheA.toFile().deleteRecursively()) {
            "Should be able to remove directory for cacheA: $cacheA"
        }
        projectDir.resolve(outputFile).toFile().delete()
        val result2 = prepare(gradleVersion, "props", "props2", "-i").build()
        if (isCI) {
            println(result2.output)
        }
        Assertions.assertEquals(TaskOutcome.FROM_CACHE, result2.task(":props")?.outcome) {
            "second execution => task should be resolved from cache"
        }
    }
}
