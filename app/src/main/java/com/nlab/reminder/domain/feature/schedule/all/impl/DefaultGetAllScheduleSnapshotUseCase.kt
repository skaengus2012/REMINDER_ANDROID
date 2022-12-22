/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.feature.schedule.all.impl

import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.common.schedule.visibleconfig.CompletedScheduleShownRepository
import com.nlab.reminder.domain.common.util.link.LinkMetadata
import com.nlab.reminder.domain.common.util.link.LinkMetadataTable
import com.nlab.reminder.domain.common.util.link.LinkMetadataTableRepository
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleSnapshot
import com.nlab.reminder.domain.feature.schedule.all.GetAllScheduleSnapshotUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

/**
 * @author thalys
 */
class DefaultGetAllScheduleSnapshotUseCase(
    scheduleRepository: ScheduleRepository,
    private val linkMetadataTableRepository: LinkMetadataTableRepository,
    private val completedScheduleShownRepository: CompletedScheduleShownRepository,
    private val scheduleUiStateFlowFactory: ScheduleUiStateFlowFactory,
) : GetAllScheduleSnapshotUseCase {
    private val findAllSchedules: Flow<List<Schedule>> =
        scheduleRepository.get(GetRequest.All)
    private val findNotCompleteSchedules: Flow<List<Schedule>> =
        scheduleRepository.get(GetRequest.ByComplete(isComplete = false))

    @OptIn(FlowPreview::class)
    override fun invoke(): Flow<AllScheduleSnapshot> =
        completedScheduleShownRepository.get().flatMapConcat(this::getSnapshot)

    private fun getSnapshot(isCompletedScheduleShown: Boolean): Flow<AllScheduleSnapshot> =
        getScheduleUiStateStream(isCompletedScheduleShown).map { scheduleUiStates ->
            AllScheduleSnapshot(
                scheduleUiStates,
                isCompletedScheduleShown
            )
        }

    private fun getScheduleStream(isCompletedScheduleShown: Boolean): Flow<List<Schedule>> =
        if (isCompletedScheduleShown) findAllSchedules
        else findNotCompleteSchedules

    private fun getScheduleUiStateStream(isCompletedScheduleShown: Boolean): Flow<List<ScheduleUiState>> =
        combine(
            getScheduleStream(isCompletedScheduleShown)
                .let(scheduleUiStateFlowFactory::with)
                .onEach { uiStates -> linkMetadataTableRepository.setLinks(uiStates.map { it.link }) },
            linkMetadataTableRepository.getStream(),
            ::combineScheduleUiStateAndLinkMetadataTable
        )

    companion object {
        private fun combineScheduleUiStateAndLinkMetadataTable(
            scheduleUiStates: List<ScheduleUiState>,
            linkMetadataTable: LinkMetadataTable
        ): List<ScheduleUiState> = scheduleUiStates.map { uiState ->
            val foundLinkMetadata: LinkMetadata? = linkMetadataTable[uiState.link]
            if (foundLinkMetadata != null) uiState.copy(linkMetadata = foundLinkMetadata)
            else uiState
        }
    }
}