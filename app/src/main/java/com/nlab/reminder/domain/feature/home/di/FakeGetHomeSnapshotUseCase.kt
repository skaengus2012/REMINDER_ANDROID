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

package com.nlab.reminder.domain.feature.home.di

import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.core.util.test.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.domain.common.tag.TagRepository
import com.nlab.reminder.domain.feature.home.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart

/**
 * @author Doohyun
 */
@Deprecated(message = "Fake UseCase was used")
class FakeGetHomeSnapshotUseCase(
    private val tagRepository: TagRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : GetHomeSnapshotUseCase {
    @ExcludeFromGeneratedTestReport
    override fun invoke(): Flow<HomeSnapshot> {
        // TODO with scheduleEntityModel
        return tagRepository.get()
            .map { tags ->
                HomeSnapshot(
                    NotificationUiState(
                        todayCount = (0..50).random().toString(),
                        timetableCount = (0..50).random().toString(),
                        allCount = (0..50).random().toString(),
                    ),
                    tags
                )
            }
            .flowOn(dispatcher)
            .onStart { delay(1_500) }
    }
}