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

package com.nlab.reminder.core.component.schedulelist.content

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.ScheduleTiming
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.model.genScheduleTiming
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.faker.genNonBlankString
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genInt
import kotlin.collections.Set

/**
 * @author Thalys
 */
fun genScheduleListResource(
    id: ScheduleId = genScheduleId(),
    title: NonBlankString = genNonBlankString(),
    note: NonBlankString? = genNonBlankString(),
    link: Link? = genLink(),
    linkMetadata: LinkMetadata? = genLinkMetadata(),
    timing: ScheduleTiming? = genScheduleTiming(),
    isComplete: Boolean = genBoolean(),
    tags: List<Tag> = genTags().toList()
): ScheduleListResource = ScheduleListResource(
    id = id,
    title = title,
    note = note,
    link = link,
    linkMetadata = linkMetadata,
    timing = timing,
    isComplete = isComplete,
    tags = tags
)

fun genScheduleListResource(
    schedule: Schedule,
    linkMetadata: LinkMetadata? = genLinkMetadata(),
    tags: List<Tag> = schedule.content.tagIds.map { genTag(it) }
): ScheduleListResource = genScheduleListResource(
    id = schedule.id,
    title = schedule.content.title,
    note = schedule.content.note,
    link = schedule.content.link,
    linkMetadata = linkMetadata,
    timing = schedule.content.timing,
    isComplete = schedule.isComplete,
    tags = tags
)

fun genScheduleListResources(
    count: Int = genInt(min = 5, max = 10)
): Set<ScheduleListResource> = (1 .. count).toSet { index ->
    genScheduleListResource(
        id = ScheduleId(index.toLong()),
        title = "Test $index".toNonBlankString()
    )
}