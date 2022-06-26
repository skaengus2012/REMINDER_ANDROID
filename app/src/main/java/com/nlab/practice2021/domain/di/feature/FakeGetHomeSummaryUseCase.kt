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

package com.nlab.practice2021.domain.di.feature

import com.nlab.practice2021.domain.common.tag.Tag
import com.nlab.practice2021.domain.feature.home.GetHomeSummaryUseCase
import com.nlab.practice2021.domain.feature.home.HomeSummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * TODO Implement with Room
 * @author Doohyun
 */
@Deprecated(message = "Fake UseCase was used")
class FakeGetHomeSummaryUseCase : GetHomeSummaryUseCase {
    override fun invoke(): Flow<HomeSummary> = flow {
        emit(HomeSummary(
            todayNotificationCount = 10,
            timetableNotificationCount = 8,
            allNotificationCount = 20,
            tags = listOf(
                Tag(
                    "Hello",
                    6
                ),
                Tag(
                    "돈내는거",
                    1
                ),
                Tag(
                    "약속",
                    2
                ),
                Tag(
                    "건강",
                    3
                ),
                Tag(
                    "공과금 내는 날~!!",
                    4
                ),
                Tag(
                    "장보러 가는 날",
                    5
                ),
                Tag(
                    "Phone",
                    6
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
                    6
                ),
                Tag(
                    "돈내는거",
                    1
                ),
                Tag(
                    "건강",
                    3
                ),
                Tag(
                    "공과금 내는 날~!!",
                    4
                ),
                Tag(
                    "장보러 가는 날",
                    5
                ),
                Tag(
                    "Phone",
                    6
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
                    6
                ),
                Tag(
                    "돈내는거",
                    1
                ),
                Tag(
                    "건강",
                    3
                ),
                Tag(
                    "공과금 내는 날~!!",
                    4
                ),
                Tag(
                    "장보러 가는 날",
                    5
                ),
                Tag(
                    "Phone",
                    6
                ),
                Tag(
                    "Phone",
                    4
                ),
                Tag(
                    "Phone",
                    2
                )
            )
        ))
    }
}