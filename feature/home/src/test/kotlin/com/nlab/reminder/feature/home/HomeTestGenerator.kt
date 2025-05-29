/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.feature.home

import com.nlab.reminder.core.component.tag.edit.TagEditState
import com.nlab.reminder.core.component.tag.edit.TagEditStateMachine
import com.nlab.reminder.core.component.tag.edit.genTagEditState
import com.nlab.reminder.core.component.usermessage.UserMessageFactory
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import io.mockk.mockk

/**
 * @author Doohyun
 */
internal fun genHomeEnvironment(
    scheduleRepository: ScheduleRepository = mockk(),
    tagRepository: TagRepository = mockk(),
    tagEditStateMachine: TagEditStateMachine = mockk(),
    userMessageFactory: UserMessageFactory = mockk()
) = HomeEnvironment(
    scheduleRepository = scheduleRepository,
    tagRepository = tagRepository,
    tagEditStateMachine = tagEditStateMachine,
    userMessageFactory = userMessageFactory
)

internal fun genHomeActionStateSynced(
    todaySchedulesCount: NonNegativeLong = genNonNegativeLong(),
    timetableSchedulesCount: NonNegativeLong = genNonNegativeLong(),
    allSchedulesCount: NonNegativeLong = genNonNegativeLong(),
    tags: List<Tag> = genTags().toList()
) = HomeAction.StateSynced(
    todaySchedulesCount = todaySchedulesCount,
    timetableSchedulesCount = timetableSchedulesCount,
    allSchedulesCount = allSchedulesCount,
    sortedTags = tags
)

internal fun genHomeUiStateSuccess(
    todayScheduleCount: NonNegativeLong = genNonNegativeLong(),
    timetableScheduleCount: NonNegativeLong = genNonNegativeLong(),
    allScheduleCount: NonNegativeLong = genNonNegativeLong(),
    tags: List<Tag> = genTags().toList(),
    tagEditState: TagEditState = genTagEditState(),
) = HomeUiState.Success(
    todayScheduleCount = todayScheduleCount,
    timetableScheduleCount = timetableScheduleCount,
    allScheduleCount = allScheduleCount,
    tags = tags,
    tagEditState = tagEditState
)