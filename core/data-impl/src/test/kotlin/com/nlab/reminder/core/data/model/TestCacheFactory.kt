/*
 * Copyright (C) 2024 The N's lab Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.foundation.cache.Cache
import com.nlab.reminder.core.foundation.cache.CacheFactory

/**
 * Facade for using cacheFactory for testing.
 * Currently expected to be used primarily in data-impl, added here.
 * If you need to use it in another module, create `foundation-impl`!
 *
 * @author Doohyun
 */
@Suppress("UNCHECKED_CAST")
internal class TestCacheFactory(
    private val transform: (key: Any) -> Any
) : CacheFactory {
    override fun <K : Any, V : Any> create(
        maxSize: Int,
        createValueIfNeeded: (K) -> V
    ): Cache<K, V> = object : Cache<K, V> {
        override fun get(key: K): V = transform(key) as V
    }
}