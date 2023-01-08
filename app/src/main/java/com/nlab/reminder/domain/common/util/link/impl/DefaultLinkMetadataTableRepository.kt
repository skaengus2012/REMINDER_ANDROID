/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.common.util.link.impl

import com.nlab.reminder.core.kotlin.collection.filter
import com.nlab.reminder.core.kotlin.util.onSuccess
import com.nlab.reminder.domain.common.util.link.LinkMetadataRepository
import com.nlab.reminder.domain.common.util.link.LinkMetadataTable
import com.nlab.reminder.domain.common.util.link.LinkMetadataTableRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * @author thalys
 */
class DefaultLinkMetadataTableRepository(
    private val linkMetadataRepository: LinkMetadataRepository,
    private val coroutineScope: CoroutineScope
) : LinkMetadataTableRepository {
    private val linkMetadataStream = MutableStateFlow<LinkMetadataTable>(emptyMap())

    override fun getStream(): StateFlow<LinkMetadataTable> = linkMetadataStream.asStateFlow()

    override suspend fun setLinks(links: List<String>)  {
        val curMetadataTable: LinkMetadataTable = linkMetadataStream.value
        val noneCacheLinks: List<String> = links
            .filter(String::isNotBlank)
            .filter { link -> link !in curMetadataTable }
            .distinct()

        noneCacheLinks.forEach { link ->
            coroutineScope.launch {
                linkMetadataRepository.get(link)
                    .onSuccess { linkMetadata ->
                        linkMetadataStream.update { old -> old + (link to linkMetadata) }
                    }
            }
        }
    }
}