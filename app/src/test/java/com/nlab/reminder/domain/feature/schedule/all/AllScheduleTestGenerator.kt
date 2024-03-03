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

package com.nlab.reminder.domain.feature.schedule.all

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.schedule.model.ScheduleElement
import com.nlab.reminder.core.schedule.model.genScheduleElements
import com.nlab.testkit.genBoolean
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * @author Doohyun
 */
internal fun genAllScheduleUiState(): AllScheduleUiState = when (genBoolean()) {
    true -> AllScheduleUiState.Empty
    false -> genAllScheduleUiStateLoaded()
}

internal fun genAllScheduleUiStateLoaded(
    scheduleElements: ImmutableList<ScheduleElement> = genScheduleElements().toImmutableList(),
    isCompletedScheduleShown: Boolean = genBoolean(),
    isSelectionMode: Boolean = genBoolean(),
    isSelectedActionInvoked: Boolean = false,
    workflows: ImmutableList<AllScheduleWorkflow> = persistentListOf()
): AllScheduleUiState.Loaded = AllScheduleUiState.Loaded(
    scheduleElements = scheduleElements,
    isCompletedScheduleShown = isCompletedScheduleShown,
    isSelectionMode = isSelectionMode,
    isSelectedActionInvoked = isSelectedActionInvoked,
    workflows = workflows
)

internal fun genAllScheduleWorkflowLink(
    link: Link = genLink()
): AllScheduleWorkflow.LinkPage = AllScheduleWorkflow.LinkPage(link)

internal fun genAllScheduleWorkflowsExcludeEmpty(): List<AllScheduleWorkflow> = listOf(
    genAllScheduleWorkflowLink()
)

internal fun genAllScheduleSelectedAction(): AllScheduleAction =
    listOf(
        AllScheduleAction.OnSelectedSchedulesCompleteClicked(isComplete = genBoolean())
    )
        .shuffled()
        .first()