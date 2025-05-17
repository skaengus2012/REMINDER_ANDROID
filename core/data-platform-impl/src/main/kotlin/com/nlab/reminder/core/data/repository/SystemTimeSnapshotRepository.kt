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

package com.nlab.reminder.core.data.repository

import com.nlab.reminder.core.data.model.TimeSnapshot
import com.nlab.reminder.core.data.util.SystemTimeChangedMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Clock

/**
 * @author Doohyun
 */
class SystemTimeSnapshotRepository(
    private val systemTimeChangedMonitor: SystemTimeChangedMonitor
) : TimeSnapshotRepository {
    override fun getNowSnapshotAsStream(): Flow<TimeSnapshot> {
        return systemTimeChangedMonitor
            .timeChangedEvent
            .onStart { emit(Unit) }
            .map { TimeSnapshot(value = Clock.System.now(), fromRemote = false) }
    }
}