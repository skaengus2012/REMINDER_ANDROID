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
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.kotlin.PositiveInt
import com.nlab.reminder.core.kotlin.concurrent.atomics.update
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf
import kotlin.concurrent.atomics.AtomicReference

/**
 * @author Doohyun
 */
class LinkMetadataRemoteCache(private val cacheSize: PositiveInt) {
    private var table = AtomicReference<PersistentMap<Link, LinkMetadata>>(persistentMapOf())

    fun snapshot(): Map<Link, LinkMetadata> = table.load()

    fun put(link: Link, linkMetadata: LinkMetadata) {
        table.update { cur ->
            cur.mutate { mutable ->
                mutable -= link
                mutable[link] = linkMetadata
                if (mutable.size > cacheSize.value) {
                    mutable -= mutable.keys.first()
                }
            }
        }
    }
}