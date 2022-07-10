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

package com.nlab.reminder.domain.internal.di.feature.home

import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.TagStyleResource
import com.nlab.reminder.domain.feature.home.GetHomeSummaryUseCase
import com.nlab.reminder.domain.feature.home.HomeSummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * TODO Implement with Room
 * @author Doohyun
 */
@Deprecated(message = "Fake UseCase was used")
class FakeGetHomeSummaryUseCase : GetHomeSummaryUseCase {
    override suspend fun invoke(): Flow<HomeSummary> = flow {
        emit(HomeSummary(
            todayNotificationCount = 10,
            timetableNotificationCount = 8,
            allNotificationCount = 20,
            tags = listOf(
                Tag(
                    "Hello",
                    TagStyleResource.TYPE6,
                    usageCount = 10
                ),
                Tag(
                    "돈내는거",
                    TagStyleResource.TYPE1,
                    usageCount = 5
                ),
                Tag(
                    "약속",
                    TagStyleResource.TYPE2,
                    usageCount = 8
                ),
                Tag(
                    "건강",
                    TagStyleResource.TYPE3,
                    usageCount = 9
                ),
                Tag(
                    "공과금 내는 날~!!",
                    TagStyleResource.TYPE4,
                    usageCount = 1
                ),
                Tag(
                    "장보러 가는 날",
                    TagStyleResource.TYPE5,
                    usageCount = 1
                ),
                Tag(
                    "Phone",
                    TagStyleResource.TYPE6,
                    usageCount = 9
                )
            )
        ))

        delay(1500)

        emit(HomeSummary(
            todayNotificationCount = 16,
            timetableNotificationCount = 8,
            allNotificationCount = 20,
            tags = listOf(
                Tag(
                    "Hello",
                    TagStyleResource.TYPE6,
                    usageCount = 10
                ),
                Tag(
                    "돈내는거",
                    TagStyleResource.TYPE1,
                    usageCount = 5
                ),
                Tag(
                    "건강",
                    TagStyleResource.TYPE3,
                    usageCount = 9
                ),
                Tag(
                    "공과금 내는 날~!!",
                    TagStyleResource.TYPE4,
                    usageCount = 19
                ),
                Tag(
                    "장보러 가는 날",
                    TagStyleResource.TYPE5,
                    usageCount = 2
                ),
                Tag(
                    "Phone",
                    TagStyleResource.TYPE6,
                    usageCount = 9
                )
            )
        ))

        delay(2000)

        emit(HomeSummary(
            todayNotificationCount = 16,
            timetableNotificationCount = 8,
            allNotificationCount = 20,
            tags = listOf(
                Tag(
                    "Hello",
                    TagStyleResource.TYPE6,
                    usageCount = 9
                ),
                Tag(
                    "돈내는거",
                    TagStyleResource.TYPE1,
                    usageCount = 9
                ),
                Tag(
                    "건강",
                    TagStyleResource.TYPE3,
                    usageCount = 9
                ),
                Tag(
                    "공과금 내는 날~!!",
                    TagStyleResource.TYPE4,
                    usageCount = 9
                ),
                Tag(
                    "장보러 가는 날",
                    TagStyleResource.TYPE5,
                    usageCount = 9
                ),
                Tag(
                    "Phone",
                    TagStyleResource.TYPE6,
                    usageCount = 10
                ),
                Tag(
                    "Phone",
                    TagStyleResource.TYPE3,
                    usageCount = 20
                ),
                Tag(
                    "Phone",
                    TagStyleResource.TYPE2,
                    usageCount = 25
                ),
                Tag(
                    "돈내는거",
                    TagStyleResource.TYPE6,
                    usageCount = 20
                ),
                Tag(
                    "건강",
                    TagStyleResource.TYPE3,
                    usageCount = 19
                ),
                Tag(
                    "공과금 내는 날~!!",
                    TagStyleResource.TYPE2,
                    usageCount = 16
                ),
            )
        ))
    }
}