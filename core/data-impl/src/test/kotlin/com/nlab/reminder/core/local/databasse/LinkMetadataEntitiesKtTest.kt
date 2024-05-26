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

package com.nlab.reminder.core.local.databasse

import com.nlab.reminder.core.data.local.database.toEntity
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.testkit.faker.genLong
import org.junit.Test

/**
 * @author Doohyun
 */
internal class LinkMetadataEntitiesKtTest {
    @Test
    fun testToEntity() {
        val link = genLink()
        val timeStamp = genLong()
        val linkMetadata = genLinkMetadata()
        val entity = linkMetadata.toEntity(link, timeStamp)

        assert(entity.link == link.value)
        assert(entity.timestamp == timeStamp)
        assert(entity.title == linkMetadata.title)
        assert(entity.imageUrl == linkMetadata.imageUrl)
    }
}