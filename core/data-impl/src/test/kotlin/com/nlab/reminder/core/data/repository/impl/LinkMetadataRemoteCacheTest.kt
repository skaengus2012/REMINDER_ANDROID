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

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.genLinkAndMetadataAndEntity
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toPositiveInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class LinkMetadataRemoteCacheTest {
    @Test
    fun `Given entry and empty cache, When put entry, Then cache contains the entry`() {
        // given
        val (link, linkMetadata) = genLinkAndMetadataAndEntity()
        val cache = LinkMetadataRemoteCache(cacheSize = 1.toPositiveInt())
        cache.put(link, linkMetadata)
        assertThat(
            cache.snapshot(),
            equalTo(mapOf(link to linkMetadata))
        )
    }

    @Test
    fun `Given multiple entries and single size cache, When put entries, Then oldest entry is evicted`() {
        val linkAndMetadataList = List(2) { index ->
            genLinkAndMetadataAndEntity(
                link = Link(index.toString().toNonBlankString())
            )
        }
        val cache = LinkMetadataRemoteCache(cacheSize = 1.toPositiveInt())
        linkAndMetadataList.forEach { (link, linkMetadata) ->
            cache.put(link, linkMetadata)
        }
        assertThat(
            cache.snapshot(),
            equalTo(linkAndMetadataList.last().let { (link, linkMetadata) -> mapOf(link to linkMetadata) })
        )
    }
}