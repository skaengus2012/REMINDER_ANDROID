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

package com.nlab.reminder.core.schedule.model

import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * @author Doohyun
 */
fun genScheduleElement(
    schedule: Schedule = genSchedule(),
    isCompleteMarked: Boolean = genBoolean(),
    linkMetadata: LinkMetadata? = genLinkMetadata()
) = ScheduleElement(schedule, isCompleteMarked, linkMetadata)

fun genScheduleElements(size: Int = genInt(min = 2, max = 10)): List<ScheduleElement> =
    List(size) { genScheduleElement() }

fun Schedule.mapToScheduleElementsAsImmutableList(
    isCompleteMarked: Boolean = genBoolean(),
    linkMetadata: LinkMetadata? = genLinkMetadata()
): ImmutableList<ScheduleElement> =
    persistentListOf(genScheduleElement(schedule = this, isCompleteMarked, linkMetadata))

fun List<Schedule>.mapToScheduleElementsAsImmutableList(
    isCompleteMarked: Boolean = genBoolean(),
    linkMetadata: LinkMetadata? = genLinkMetadata()
): ImmutableList<ScheduleElement> = map { genScheduleElement(it, isCompleteMarked, linkMetadata) }.toImmutableList()