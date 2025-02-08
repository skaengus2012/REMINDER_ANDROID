/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.testkit.faker.genIntGreaterThanZero
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class InMemoryLinkMetadataCacheTest {
    @Test
    fun `Given initialCache, When create with initialCache, Then tableFlow contains initialCache`() {
        val initialCache = buildMap {
            repeat(genIntGreaterThanZero(max = 10)) {
                put(genLink(), genLinkMetadata())
            }
        }
        val cache = InMemoryLinkMetadataCache(initialCache)

        assertThat(cache.tableFlow.value, equalTo(initialCache))
    }

    @Test
    fun `Given link, metadata and empty initial cache, When put, Then snapshot equals given link, metadata`()  {
        val link = genLink()
        val metadata = genLinkMetadata()

        val cache = InMemoryLinkMetadataCache(emptyMap())
        val snapshot = cache.put(link, metadata)

        assertThat(snapshot, equalTo(mapOf(link to metadata)))
    }

    @Test
    fun `Given many link, metadata sets and empty initial cache, When putAll, Then snapshot equals given link, metadata`() {
        val input = buildMap {
            repeat(genIntGreaterThanZero(max = 10)) {
                put(genLink(), genLinkMetadata())
            }
        }
        val cache = InMemoryLinkMetadataCache(emptyMap())
        val snapshot = cache.putAll(input)

        assertThat(snapshot, equalTo(input))
    }
}