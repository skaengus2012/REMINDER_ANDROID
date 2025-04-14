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

import com.nlab.reminder.core.kotlin.NonNegativeInt
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.toNonNegativeInt
import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.model.TagEntity
import com.nlab.testkit.faker.genInt

typealias TagAndEntity = Pair<Tag, TagEntity>

/**
 * @author Doohyun
 */
fun genTagAndEntity(tag: Tag = genTag()): TagAndEntity = tag to TagEntity(tag.id.rawId, tag.name.value)

fun genTagAndEntities(count: Int = genInt(min = 5, max = 10)): List<TagAndEntity> = List(count) { index ->
    genTagAndEntity(tag = genTag(id = TagId(index.toLong())))
}

fun genTagUsages(
    tags: Collection<Tag>,
    maxUsageCount: NonNegativeInt = 20.toNonNegativeInt()
): Set<TagUsage> = tags.toSet { tag ->
    TagUsage(
        tag = tag,
        usageCount = genInt(min = 0, max = maxUsageCount.value).toNonNegativeInt()
    )
}

fun Set<TagUsage>.toScheduleTagListEntities(): Set<ScheduleTagListEntity> {
    val entities = flatMap { tagUsage ->
        List(tagUsage.usageCount.value) {
            ScheduleTagListEntity(
                scheduleId = it.toLong() + 1,
                tagId = tagUsage.tag.id.rawId
            )
        }
    }
    return entities.toSet()
}