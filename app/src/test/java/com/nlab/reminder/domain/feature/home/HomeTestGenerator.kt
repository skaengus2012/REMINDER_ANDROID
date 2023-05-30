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

import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.genTags
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genInt
import kotlinx.collections.immutable.toPersistentList

/**
 * @author Doohyun
 */
internal fun genHomeUiStateSuccess(
    todayScheduleCount: Int = genInt(),
    todayScheduleShown: Boolean = genBoolean(),
    timetableScheduleCount: Int = genInt(),
    timetableScheduleShown: Boolean = genBoolean(),
    allScheduleCount: Int = genInt(),
    allScheduleShown: Boolean = genBoolean(),
    tags: List<Tag> = genTags()
): HomeUiState.Success = HomeUiState.Success(
    todayScheduleCount = todayScheduleCount,
    todayScheduleShown = todayScheduleShown,
    timetableScheduleCount = timetableScheduleCount,
    timetableScheduleShown = timetableScheduleShown,
    allScheduleCount = allScheduleCount,
    allScheduleShown = allScheduleShown,
    tags = tags.toPersistentList()
)