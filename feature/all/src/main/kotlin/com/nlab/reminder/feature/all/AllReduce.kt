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

import com.nlab.reminder.core.component.schedulelist.content.clear
import com.nlab.statekit.dsl.reduce.DslReduce
import com.nlab.statekit.reduce.Reduce
import com.nlab.reminder.feature.all.AllAction.*
import com.nlab.reminder.feature.all.AllUiState.*

internal typealias AllReduce = Reduce<AllAction, AllUiState>

/**
 * @author Thalys
 */
internal fun AllReduce(environment: AllEnvironment): AllReduce = DslReduce {
    actionScope<StateSynced> {
        transition<Loading> {
            Success(
                entryAt = action.entryAt,
                scheduleListResources = action.scheduleResources,
                completedScheduleVisible = action.completedScheduleVisible,
                multiSelectionEnabled = false
            )
        }
        transition<Success> {
            current.copy(
                entryAt = action.entryAt,
                completedScheduleVisible = action.completedScheduleVisible,
                scheduleListResources = action.scheduleResources,
            )
        }
    }
    stateScope<Success> {
        suspendEffect<OnCompletedScheduleVisibilityToggled> {
            environment.completedScheduleShownRepository.setShown(isShown = current.completedScheduleVisible.not())
        }
        transition<OnSelectionModeToggled> {
            current.copy(multiSelectionEnabled = current.multiSelectionEnabled.not())
        }
        effect<OnSelectionModeToggled> {
            if (current.multiSelectionEnabled) {
                environment.userSelectedSchedulesStore.clear()
            }
        }
        effect<OnItemSelectionUpdated> {
            environment.userSelectedSchedulesStore.replace(action.selectedIds)
        }
        suspendEffect<OnItemCompletionUpdated> {
            println("Hello OnItemCompletionUpdated ${action.scheduleId} ${action.targetCompleted}")
            environment.updateScheduleCompletion(
                scheduleId = action.scheduleId,
                targetCompleted = action.targetCompleted
            )
        }
    }
}