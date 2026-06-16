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

package com.nlab.reminder.feature.all

import com.nlab.reminder.core.component.schedulelist.clear
import com.nlab.reminder.core.data.model.ScheduleContent
import com.nlab.reminder.core.data.repository.DeleteScheduleQuery
import com.nlab.reminder.core.data.repository.SaveScheduleQuery
import com.nlab.reminder.core.data.repository.UpdateAllScheduleQuery
import com.nlab.reminder.core.kotlin.onFailure
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import com.nlab.statekit.dsl.reduce.DslReduce
import com.nlab.statekit.reduce.Reduce
import com.nlab.reminder.feature.all.AllAction.*
import com.nlab.reminder.feature.all.AllUiState.*

internal typealias AllReduce = Reduce<AllAction, AllUiState>

/**
 * @author Thalys
 */
internal fun AllReduce(environment: AllEnvironment): AllReduce = DslReduce {
    actionScope<StateSyncCompleted> {
        transition<Loading> {
            Success(
                entryAt = action.entryAt,
                scheduleResources = action.userScheduleListResourceReport.userScheduleListResources,
                scheduleListStats = action.userScheduleListResourceReport.scheduleListStats,
                menuExpanded = false,
                selectionEnabled = false,
                showCompletedSchedulesCleanupConfirmation = false,
                showSelectedSchedulesDeletionConfirmation = false,
                replayStamp = 0,
            )
        }
        transition<Success> {
            current.copy(
                entryAt = action.entryAt,
                scheduleResources = action.userScheduleListResourceReport.userScheduleListResources,
                scheduleListStats = action.userScheduleListResourceReport.scheduleListStats,
                replayStamp = 0,
            )
        }
    }
    stateScope<Success> {
        transition<ScheduleRestoreRequested> {
            when {
                current.replayStamp != action.prevReplayStamp -> current
                current.scheduleResources != action.prevScheduleResources -> current
                else -> current.copy(replayStamp = current.replayStamp + 1)
            }
        }

        suspendEffect<CompletedSchedulesToggleClicked> {
            environment.completedScheduleShownRepository.setShown(isShown = action.visible)
        }

        transition<CompletedSchedulesCleanupClicked> {
            current.copy(showCompletedSchedulesCleanupConfirmation = true)
        }

        transition<CleanupConfirmAnswered> {
            current.copy(showCompletedSchedulesCleanupConfirmation = false)
        }

        suspendEffect<CleanupConfirmAnswered> {
            if (action.confirmed) {
                environment.scheduleRepository
                    .delete(DeleteScheduleQuery.ByComplete(isComplete = true))
                    .onFailure { t ->
                        // TODO Handle failure
                    }
            }
        }

        suspendEffect<StateSyncCompleted> {
            if (current.selectionEnabled.not()) return@suspendEffect

            val isChanged = environment.isScheduleListResourceChanged(
                oldElements = current.scheduleResources,
                newElements = action.userScheduleListResourceReport.userScheduleListResources
            )
            if (isChanged) {
                dispatch(SelectionModeChangeRequested(enabled = false))
            }
        }

        suspendEffect<SelectionModeClicked> {
            dispatch(SelectionModeChangeRequested(enabled = action.enabled))
        }

        transition<SelectionModeChangeRequested> {
            current.copy(selectionEnabled = action.enabled)
        }

        effect<SelectionModeChangeRequested> {
            if (action.enabled.not()) {
                environment.userSelectedSchedulesStore.clear()
            }
        }

        transition<MenuClicked> { current.copy(menuExpanded = true) }

        transition<MenuDropdownDismissed> { current.copy(menuExpanded = false) }

        effect<ItemSelectionUpdated> {
            environment.userSelectedSchedulesStore.replace(action.selectedIds)
        }

        suspendEffect<ItemCompletionUpdated> {
            environment.updateScheduleCompletion(
                scheduleId = action.scheduleId,
                targetCompleted = action.targetCompleted
            )
        }

        suspendEffect<ItemPositionUpdated> {
            val snapshot = action.snapshot

            val maxUncompletedIndex = snapshot.indexOfLast { it.schedule.isComplete.not() }
            val minCompletedIndex = snapshot.indexOfFirst { it.schedule.isComplete }
            if (maxUncompletedIndex != -1 && minCompletedIndex != -1 && maxUncompletedIndex >= minCompletedIndex) {
                dispatch(
                    action = ScheduleRestoreRequested(
                        prevScheduleResources = current.scheduleResources,
                        prevReplayStamp = current.replayStamp
                    )
                )
                return@suspendEffect
            }

            val completedScheduleIds =
                if (minCompletedIndex == -1) emptyList()
                else snapshot.mapNotNull { resource -> resource.takeIf { it.schedule.isComplete }?.schedule?.id }
            val uncompletedScheduleIds =
                if (maxUncompletedIndex == -1) emptyList()
                else snapshot.mapNotNull { resource -> resource.takeIf { it.schedule.isComplete.not() }?.schedule?.id }
            environment.scheduleRepository.updateAll(
                query = UpdateAllScheduleQuery.ReorderWithCompletedGroup(
                    completedGroupSortedIds = completedScheduleIds,
                    uncompletedGroupSortedIds = uncompletedScheduleIds
                )
            )
        }

        suspendEffect<ScheduleAdditionSubmitted> {
            environment.scheduleRepository.save(
                query = SaveScheduleQuery.Add(
                    content = ScheduleContent(
                        title = action.title,
                        note = action.note.tryToNonBlankStringOrNull(),
                        link = null,
                        tagIds = emptySet(),
                        timing = null
                    )
                )
            )
            // TODO Handle failure cases from save(), e.g. network/DB I/O errors, validation failures (invalid/empty title or note), and repository constraint violations, and surface them appropriately in the UI/logs.
        }

        suspendEffect<ScheduleDeletionClicked> {
            environment.deleteSchedule(scheduleIds = setOf(action.scheduleId))
            // TODO Handle failure cases
        }

        suspendEffect<ScheduleEditSubmitted> {
            val curSchedule = current.scheduleResources
                .find { it.schedule.id == action.id }
                ?.schedule
                ?: return@suspendEffect
            val editResult = environment.editScheduleListResource(
                originResource = curSchedule,
                title = action.title,
                note = action.note,
                tagNames = action.tagNames
            )
            editResult.onFailure { t ->
                // TODO Handle failure cases from save(), e.g. network/DB I/O errors, validation failures (invalid/empty title or note), and repository constraint violations, and surface them appropriately in the UI/logs.
            }
        }

        transition<SelectedSchedulesDeletionClicked> {
            current.copy(showSelectedSchedulesDeletionConfirmation = true)
        }

        transition<SelectedSchedulesDeletionConfirmAnswered> {
            current.copy(showSelectedSchedulesDeletionConfirmation = false)
        }

        suspendEffect<SelectedSchedulesDeletionConfirmAnswered> {
            if (action.confirmed.not()) return@suspendEffect
            if (current.scheduleListStats.selectedCount.value == 0) return@suspendEffect

            environment.deleteSchedule(
                scheduleIds = current.scheduleResources
                    .mapNotNull { resource -> resource.takeIf { it.selected }?.schedule?.id }
                    .toSet()
            )
            // TODO Handle failure cases from save(), e.g. network/DB I/O errors, validation failures (invalid/empty title or note), and repository constraint violations, and surface them appropriately in the UI/logs.
        }
    }
}