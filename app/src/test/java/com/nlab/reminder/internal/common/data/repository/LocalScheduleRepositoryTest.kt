/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.internal.common.data.repository

import com.nlab.reminder.domain.common.data.model.Schedule
import com.nlab.reminder.domain.common.data.model.genScheduleId
import com.nlab.reminder.domain.common.data.model.genSchedules
import com.nlab.reminder.domain.common.data.repository.ScheduleDeleteRequest
import com.nlab.reminder.domain.common.data.repository.ScheduleGetStreamRequest
import com.nlab.reminder.domain.common.data.repository.ScheduleRepository
import com.nlab.reminder.internal.common.android.database.ScheduleDao
import com.nlab.reminder.internal.common.android.database.ScheduleEntityWithTagEntities
import com.nlab.reminder.internal.common.data.model.toEntity
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genInt
import com.nlab.testkit.genLong
import com.nlab.testkit.once
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
internal class LocalScheduleRepositoryTest {
    @Test
    fun `Get schedule stream from dao`() = runTest {
        val schedules = genSchedules()
        val scheduleEntities = schedules.map { schedule ->
            ScheduleEntityWithTagEntities(
                schedule.toEntity(),
                schedule.tags.map { it.toEntity() }
            )
        }
        val isComplete = genBoolean()

        testGet(
            scheduleDao = mock {
                whenever(mock.findAsStream()) doReturn flowOf(scheduleEntities)
            },
            request = ScheduleGetStreamRequest.All,
            expectedResult = schedules
        )
        testGet(
            scheduleDao = mock {
                whenever(mock.findByCompleteAsStream(isComplete)) doReturn flowOf(scheduleEntities)
            },
            request = ScheduleGetStreamRequest.ByComplete(isComplete),
            expectedResult = schedules
        )
    }

    @Test
    fun `When repository called delete actions, Then dao also called delete`() = runTest {
        val randomBoolean = genBoolean()
        val randomLongs = List(genInt(min = 1, max = 5)) { genLong() }

        testDelete(
            request = ScheduleDeleteRequest.ByComplete(randomBoolean),
            doVerify = { deleteByComplete(isComplete = randomBoolean) }
        )
        testDelete(
            request = ScheduleDeleteRequest.ById(genScheduleId(value = randomLongs.first())),
            doVerify = { deleteByScheduleIds(listOf(randomLongs.first())) }
        )
        testDelete(
            request = ScheduleDeleteRequest.ByIds(randomLongs.map(::genScheduleId)),
            doVerify = { deleteByScheduleIds(randomLongs) }
        )
    }
}

private fun genScheduleRepository(
    scheduleDao: ScheduleDao = mock()
): ScheduleRepository = LocalScheduleRepository(scheduleDao)

private suspend fun testGet(
    scheduleDao: ScheduleDao,
    request: ScheduleGetStreamRequest,
    expectedResult: List<Schedule>
) {
    val scheduleRepository = genScheduleRepository(scheduleDao)
    val actualSchedules = scheduleRepository.getAsStream(request)
        .take(1)
        .first()

    assertThat(actualSchedules, equalTo(expectedResult))
}

private suspend inline fun <T : ScheduleDeleteRequest> testDelete(
    request: T,
    doVerify: (ScheduleDao).(T) -> Unit
) {
    val scheduleDao = mock<ScheduleDao>()
    val scheduleRepository = genScheduleRepository(scheduleDao = scheduleDao)
    scheduleRepository.delete(request)
    verify(scheduleDao, once()).doVerify(request)
}