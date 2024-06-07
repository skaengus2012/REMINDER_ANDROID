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
import com.nlab.reminder.core.data.local.database.toModel
import com.nlab.reminder.core.data.local.database.toModels
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.model.genTags
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class TagEntitiesKtTest {
    @Test
    fun testToEntity() {
        val tag = genTag()
        val tagEntity = tag.toEntity()

        assert(tag.id.value == tagEntity.tagId)
        assert(tag.name == tagEntity.name)
    }

    @Test
    fun testToModel() {
        val expectedTag = genTag()
        val actualTag = expectedTag.toEntity().toModel()

        assertThat(
            actualTag,
            equalTo(expectedTag)
        )
    }

    @Test
    fun testToModels() {
        val expectedTags = genTags()
        val actualTags = expectedTags.map { it.toEntity() }.toModels()

        assertThat(
            actualTags,
            equalTo(expectedTags)
        )
    }
}