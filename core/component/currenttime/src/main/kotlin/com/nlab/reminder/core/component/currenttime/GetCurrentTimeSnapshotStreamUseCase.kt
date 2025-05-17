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

package com.nlab.reminder.core.component.currenttime

import com.nlab.reminder.core.data.repository.TimeSnapshotRepository
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant

/**
 * @author Doohyun
 */
class GetCurrentTimeSnapshotStreamUseCase internal constructor(
    private val timeSnapshotRepository: TimeSnapshotRepository,
    private val systemTimeUsageBroadcast: SystemTimeUsageBroadcast,
) {
    operator fun invoke(): Flow<Instant> = timeSnapshotRepository.getNowSnapshotAsStream()
        .onEach { timeSnapshot ->
            if (timeSnapshot.fromRemote.not()) {
                systemTimeUsageBroadcast.notifyEvent()
            }
        }
        .map { it.value }
}