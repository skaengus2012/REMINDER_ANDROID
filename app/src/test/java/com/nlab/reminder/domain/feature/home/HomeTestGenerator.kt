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

/**
 * @author Doohyun
 */
/**
internal fun genHomeUiStateSuccess(
    todayScheduleCount: Long = genLong(),
    timetableScheduleCount: Long = genLong(),
    allScheduleCount: Long = genLong(),
    tags: List<Tag> = genTags(),
    workflow: HomeInteraction = genHomeWorkflowExcludeEmpty(),
    userMessages: List<UserMessage> = emptyList()
): HomeUiState.Success = HomeUiState.Success(
    todayScheduleCount = todayScheduleCount,
    timetableScheduleCount = timetableScheduleCount,
    allScheduleCount = allScheduleCount,
    tags = tags.toPersistentList(),
    interaction = workflow,
    userMessages = userMessages.toPersistentList()
)

internal fun genHomeTagConfigWorkflow(
    tag: Tag = genTag(),
    usageCount: Long = genLongGreaterThanZero(),
) = HomeInteraction.TagConfig(tag, usageCount)

internal fun genHomeTagRenameWorkflow(
    tag: Tag = genTag(),
    usageCount: Long = genLongGreaterThanZero(),
    renameText: String = genBothify(),
    shouldKeyboardShown: Boolean = genBoolean()
) = HomeInteraction.TagRename(tag, usageCount, renameText, shouldKeyboardShown)


internal fun genHomeTagDeleteConfig(
    tag: Tag = genTag(),
    usageCount: Long = genLongGreaterThanZero(),
) = HomeInteraction.TagDelete(tag, usageCount)

private fun genHomeWorkflowsExcludeEmpty(): List<HomeInteraction> = listOf(
    HomeInteraction.TodaySchedule,
    HomeInteraction.TimetableSchedule,
    HomeInteraction.AllSchedule,
    genHomeTagConfigWorkflow(),
    genHomeTagRenameWorkflow(),
    genHomeTagDeleteConfig()
)

internal fun genHomeWorkflowExcludeEmpty(ignoreCases: Set<KClass<out HomeInteraction>> = emptySet()): HomeInteraction =
    genHomeWorkflowsExcludeEmpty()
        .filterNot { it::class in ignoreCases }
        .random()
 */