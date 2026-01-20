/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.model.genScheduleTiming
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.faker.genNonBlankString
import com.nlab.testkit.faker.genBoolean

/**
 * @author Thalys
 */
fun genScheduleListResource(
    id: ScheduleId = genScheduleId(),
    isComplete: Boolean = genBoolean(),
    title: NonBlankString = genNonBlankString(),
    note: NonBlankString? = genNonBlankString(),
    tags: List<Tag> = genTags().toList()
): ScheduleListResource = ScheduleListResource(
    id = id,
    title = title,
    note = note,
    link = genLink(),
    linkMetadata = genLinkMetadata(),
    timing = genScheduleTiming(),
    isComplete = isComplete,
    tags = tags
)