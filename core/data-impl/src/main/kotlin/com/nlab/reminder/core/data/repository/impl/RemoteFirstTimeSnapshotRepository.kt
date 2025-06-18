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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.TimeSnapshot
import com.nlab.reminder.core.data.repository.TimeSnapshotRepository
import com.nlab.reminder.core.kotlin.getOrNull
import com.nlab.reminder.core.kotlinx.coroutines.flow.flatMapConcat
import com.nlab.reminder.core.network.datasource.TrustedTimeDataSource
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * @author Doohyun
 */
class RemoteFirstTimeSnapshotRepository(
    private val trustedTimeDataSource: TrustedTimeDataSource,
    private val fallbackSnapshotRepository: TimeSnapshotRepository,
) : TimeSnapshotRepository {
    override fun getNowSnapshotAsStream() = flow { emit(trustedTimeDataSource.getCurrentTime()) }.flatMapConcat { ret ->
        val trustedTimeValue = ret.getOrNull()
        if (trustedTimeValue == null) fallbackSnapshotRepository.getNowSnapshotAsStream()
        else flowOf(TimeSnapshot(trustedTimeValue, fromRemote = true))
    }
}