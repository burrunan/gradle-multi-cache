# Gradle Multi Cache

[![Gradle plugin](https://img.shields.io/badge/plugins.gradle.org-com.github.burrunan.multi-cache-blue.svg)](https://plugins.gradle.org/plugin/com.github.burrunan.multi-cache)
[![CI Status](https://github.com/burrunan/gradle-multi-cache/workflows/CI/badge.svg)](https://github.com/burrunan/gradle-multi-cache/actions)

This plugin allows you to configure multiple remote build caches for Gradle 5.0+

Sample use case: you want to use [S3 build cache](https://github.com/burrunan/gradle-s3-build-cache),
 and you want to use [GitHub Actions Cache](https://github.com/burrunan/gradle-cache-action) as a remote cache as well.

That setup would provide the following features:

- S3 build cache would make developer's builds faster
- GitHub Actions cache would put cache closer to the runner (faster caching), and it would reduce
your S3 bill (less objects would be queried from S3)

## Compatibility

* Version 1.0 - Gradle 5.0+

## Use in your project

If you use [gradle-cache-action](https://github.com/burrunan/gradle-cache-action),
then it configures `gradle-multi-cache` automatically.
Note: you need to use `gradle-cache-action/arguments` parameter, so `gradle-cache-action` executes your build. 

Below instructions are for advanced use cases only.

Gradle 6.6 does not support multiple remote build caches yet, so the use is a bit awkward.

Here's sample configuration:

`settings.gradle`

```gradle
buildCache {
    // Configure the first remote cache
    remote(org.gradle.caching.local.DirectoryBuildCache) {
        push = true
        directory = projectDir.resolve("cache-a").toString()
    }
    // Stash the cache to multicache configuartion
    // multicache is an extension of type com.github.burrunan.multicache.MulticacheExtension
    multicache.push("cacheA")

    // Configure the second remote cache
    // You can configure as many caches as you need
    remote(org.gradle.caching.local.DirectoryBuildCache) {
        push = true
        directory = projectDir.resolve("cache-b").toString()
    }

    // Finally you need to configure multicache
    // pushAndConfigure(x) is the same as push(x); configure { ... } 
    multicache.pushAndConfigure("cacheB") {
        // Load items sequentially: cacheB, cacheA
        // Write items concurrently to all the caches
        loadSequentiallyWriteConcurrently("cacheB", "cacheA")
    }
}
```

Please open an [issue](https://github.com/burrunan/gradle-multi-cache/issues) if you find a bug or 
have an idea for an improvement.


### Apply plugin

The Gradle build cache needs to be configured on the Settings level. As a first step, add a
dependency to the plugin to your `settings.gradle` file. Get the latest version from [Gradle plugin portal](https://plugins.gradle.org/plugin/com.github.burrunan.multi-cache).

```
plugins {
  id("com.github.burrunan.multi-cache") version "1.0"
}
```

### Configuration

- `multicache.push(cacheName: String)`

    Stashes the cache for later reuse in `multicache.configure()` or `multicache.pushAndConfigure()`. 

- `loadSequentiallyWriteConcurrently(vararg cacheNames: String)`

    Configures the list of caches to be used in the multi-cache configuration.

Note: the general recommendation is that you enable local build cache if you use a remote one, otherwise
you might end up fetching the same objects from the remote cache again and again.

More details about configuring the Gradle build cache can be found in the
[official Gradle documentation](https://docs.gradle.org/current/userguide/build_cache.html#sec:build_cache_configure).

## Contributing

Contributions are always welcome! If you'd like to contribute (and we hope you do) please open a pull request.

## Author

Vladimir Sitnikov

## License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
