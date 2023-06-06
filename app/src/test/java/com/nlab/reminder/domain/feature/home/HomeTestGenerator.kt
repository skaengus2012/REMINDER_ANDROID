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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.state.UserMessage
import com.nlab.reminder.domain.common.data.model.*
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genLong
import kotlinx.collections.immutable.toPersistentList

/**
 * @author Doohyun
 */
internal fun genHomeUiStateSuccess(
    todayScheduleCount: Long = genLong(),
    timetableScheduleCount: Long = genLong(),
    allScheduleCount: Long = genLong(),
    tags: List<Tag> = genTags(),
    todayScheduleShown: Boolean = genBoolean(),
    timetableScheduleShown: Boolean = genBoolean(),
    allScheduleShown: Boolean = genBoolean(),
    tagConfigTarget: TagConfig? = null,
    tagRenameTarget: TagRenameConfig? = null,
    tagDeleteTarget: TagDeleteConfig? = null,
    userMessages: List<UserMessage> = emptyList()
): HomeUiState.Success = HomeUiState.Success(
    todayScheduleCount = todayScheduleCount,
    timetableScheduleCount = timetableScheduleCount,
    allScheduleCount = allScheduleCount,
    tags = tags.toPersistentList(),
    todayScheduleShown = todayScheduleShown,
    timetableScheduleShown = timetableScheduleShown,
    allScheduleShown = allScheduleShown,
    tagConfigTarget = tagConfigTarget,
    tagRenameTarget = tagRenameTarget,
    tagDeleteTarget = tagDeleteTarget,
    userMessages = userMessages.toPersistentList()
)