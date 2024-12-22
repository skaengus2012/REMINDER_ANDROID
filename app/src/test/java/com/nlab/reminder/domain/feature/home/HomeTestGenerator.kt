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

import com.nlab.reminder.core.component.tag.edit.TagEditDelegate
import com.nlab.reminder.core.component.tag.edit.genTagEditState
import com.nlab.reminder.core.component.text.UiText
import com.nlab.reminder.core.component.text.genUiTexts
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.testkit.faker.requireSample
import com.nlab.testkit.faker.requireSampleExcludeTypeOf
import org.mockito.kotlin.mock
import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
internal fun genHomeEnvironment(
    tagEditDelegate: TagEditDelegate = mock(),
    scheduleRepository: ScheduleRepository = mock(),
    tagRepository: TagRepository = mock()
) = HomeEnvironment(
    tagEditDelegate = tagEditDelegate,
    scheduleRepository = scheduleRepository,
    tagRepository = tagRepository
)

internal fun genHomeActionStateSynced(
    todaySchedulesCount: NonNegativeLong = genNonNegativeLong(),
    timetableSchedulesCount: NonNegativeLong = genNonNegativeLong(),
    allSchedulesCount: NonNegativeLong = genNonNegativeLong(),
    tags: List<Tag> = genTags()
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
    tags: List<Tag> = genTags(),
    interaction: HomeInteraction = genHomeInteraction(),
    userMessages: List<UiText> = genUiTexts()
) = HomeUiState.Success(
    todayScheduleCount = todayScheduleCount,
    timetableScheduleCount = timetableScheduleCount,
    allScheduleCount = allScheduleCount,
    tags = tags,
    interaction = interaction,
    userMessages = userMessages
)

private val sampleHomeInteractions get() =  listOf(
    HomeInteraction.Empty,
    HomeInteraction.TagEdit(genTagEditState())
)

internal fun genHomeInteraction(): HomeInteraction =
    sampleHomeInteractions.requireSample()

internal fun genHomeInteractionWithExcludeTypes(
    firstType: KClass<out HomeInteraction>,
    vararg anotherTypes: KClass<out HomeInteraction>
): HomeInteraction =
    sampleHomeInteractions.requireSampleExcludeTypeOf(
        buildList {
            add(firstType)
            addAll(anotherTypes)
        }
    )