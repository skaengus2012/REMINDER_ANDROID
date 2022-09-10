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
import com.nlab.reminder.core.util.test.annotation.Generated
import com.nlab.reminder.domain.common.tag.TagRepository
import com.nlab.reminder.domain.common.tag.TagStyleResource
import com.nlab.reminder.domain.feature.home.GetHomeSnapshotUseCase
import com.nlab.reminder.domain.feature.home.HomeSnapshot
import com.nlab.reminder.domain.feature.home.TagWithResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * @author Doohyun
 */
@Deprecated(message = "Fake UseCase was used")
class FakeGetHomeSnapshotUseCase(
    private val tagRepository: TagRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : GetHomeSnapshotUseCase {
    @Generated
    override suspend fun invoke(): Flow<HomeSnapshot> {
        // TODO with scheduleEntityModel
        return tagRepository.get()
            .map { tags ->
                HomeSnapshot(
                    todayNotificationCount = (0..50).random().toLong(),
                    timetableNotificationCount = (0..50).random().toLong(),
                    allNotificationCount = (0..50).random().toLong(),
                    tags = tags.map { tag ->
                        TagWithResource(
                            tag,
                            TagStyleResource.findByCode((1..6).random())
                        )
                    }
                )
            }
            .flowOn(dispatcher)
    }
}