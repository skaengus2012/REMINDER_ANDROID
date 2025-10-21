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

package com.nlab.reminder.feature.all.ui

import com.nlab.reminder.core.component.schedulelist.content.ScheduleListResource
import com.nlab.reminder.core.component.schedulelist.content.UserScheduleListResource
import com.nlab.reminder.core.component.schedulelist.content.ui.AddLine
import com.nlab.reminder.core.component.schedulelist.content.ui.ScheduleListItem
import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.Repeat
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.ScheduleTiming
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import com.nlab.reminder.core.translation.StringIds
import kotlin.time.Clock

/**
 * @author Doohyun
 */
object FakeData {
    val testItems: List<ScheduleListItem> by lazy {
        val imageSource = listOf(
            "https://i.namu.wiki/i/RyUyEbJKhi1iuG8y26lKjvMqjX8VzFUsk82z-9gqjV3KuIGg0krkOtcoZ69nvFREm9cuPbqQA7LSTt-LEfRjKA.webp",
            "https://img.kbs.co.kr/kbs/620/news.kbs.co.kr/data/fckeditor/new/image/2023/01/13/299931673597441715.png",
            "https://img.sbs.co.kr/newimg/news/20240726/201963680.jpg",
            null
        )

        buildList {
            this += ScheduleListItem.Headline(StringIds.label_all)
            this += ScheduleListItem.HeadlinePadding
            this += ScheduleListItem.GroupHeader(
                title = "어제",
                subTitle = "Hello 어제"
            )
            repeat(times = 10) {
                this += ScheduleListItem.Content(
                    schedule = UserScheduleListResource(
                        resource = ScheduleListResource(
                            id = ScheduleId(it.toLong()),
                            title = "Title $it".toNonBlankString(),
                            note = "note $it".toNonBlankString(),
                            link = Link("https://www.naver.com/".toNonBlankString()),
                            linkMetadata = imageSource.shuffled().first()?.let { uri ->
                                LinkMetadata(
                                    title = "네이버".toNonBlankString(),
                                    imageUrl = uri.tryToNonBlankStringOrNull()
                                )
                            },
                            timing = ScheduleTiming(
                                triggerAt = Clock.System.now(),
                                isTriggerAtDateOnly = false,
                                repeat = Repeat.Hourly(interval = 5.toPositiveInt())
                            ),
                            defaultVisiblePriority = it.toNonNegativeLong(),
                            isComplete = false,
                            tags = listOf(
                                Tag(
                                    id = TagId(1),
                                    name = "여행".toNonBlankString()
                                ),
                                Tag(
                                    id = TagId(2),
                                    name = "공부".toNonBlankString()
                                ),
                                Tag(
                                    id = TagId(3),
                                    name = "이것은태그입니다만".toNonBlankString()
                                ),
                            )
                        )
                    ),
                    isLineVisible = true
                )
            }
            this += ScheduleListItem.SubGroupHeader(
                title = "오늘"
            )
            repeat(times = 10) {
                this += ScheduleListItem.Content(
                    schedule = UserScheduleListResource(
                        resource = ScheduleListResource(
                            id = ScheduleId(it.toLong()),
                            title = "Title $it".toNonBlankString(),
                            note = "note $it".toNonBlankString(),
                            link = Link("https://www.naver.com/".toNonBlankString()),
                            linkMetadata = imageSource.shuffled().first()?.let { uri ->
                                LinkMetadata(
                                    title = "네이버".toNonBlankString(),
                                    imageUrl = uri.tryToNonBlankStringOrNull()
                                )
                            },
                            timing = ScheduleTiming(
                                triggerAt = Clock.System.now(),
                                isTriggerAtDateOnly = false,
                                repeat = Repeat.Hourly(interval = 5.toPositiveInt())
                            ),
                            defaultVisiblePriority = it.toNonNegativeLong(),
                            isComplete = false,
                            tags = listOf(
                                Tag(
                                    id = TagId(1),
                                    name = "여행".toNonBlankString()
                                ),
                                Tag(
                                    id = TagId(2),
                                    name = "공부".toNonBlankString()
                                ),
                                Tag(
                                    id = TagId(3),
                                    name = "이것은태그입니다만".toNonBlankString()
                                ),
                            )
                        )
                    ),
                    isLineVisible = true
                )
            }
            this += ScheduleListItem.FooterAdd(
                newScheduleSource = null,
                line = AddLine.Type1
            )
        }
    }
}