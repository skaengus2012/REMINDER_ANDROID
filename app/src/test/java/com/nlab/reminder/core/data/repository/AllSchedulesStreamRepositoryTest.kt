package com.nlab.reminder.core.data.repository

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genSchedules
import com.nlab.testkit.genInt
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
internal class AllSchedulesStreamRepositoryTest {
    @Test
    fun `Given completed schedule not shown, When invoked, Then get schedules stream`() = runTest {
        val isCompletedSchedulesShown = false
        val schedules = List(genInt(min = 2, max = 10)) { index ->
            genSchedule(scheduleId = ScheduleId(index.toLong()), isComplete = false)
        }
        val schedulesStreamRepository = AllSchedulesStreamRepository(
            scheduleRepository = mock {
                whenever(mock.getAsStream(ScheduleGetStreamRequest.ByComplete(isComplete = false)))
                    .doReturn(flowOf(schedules.toImmutableList()))
            },
            completedScheduleShownRepository = mock {
                whenever(mock.getAsStream()) doReturn flowOf(isCompletedSchedulesShown)
            }
        )

        assertThat(
            schedulesStreamRepository.getStream()
                .take(1)
                .first(),
            equalTo(schedules)
        )
    }

    @Test
    fun `Given all schedule shown, When invoked, Then get schedules stream`() = runTest {
        val isCompletedSchedulesShown = true
        val schedules = genSchedules()
        val schedulesStreamRepository = AllSchedulesStreamRepository(
            scheduleRepository = mock {
                whenever(mock.getAsStream(ScheduleGetStreamRequest.All))
                    .doReturn(flowOf(schedules.toImmutableList()))
            },
            completedScheduleShownRepository = mock {
                whenever(mock.getAsStream()) doReturn flowOf(isCompletedSchedulesShown)
            }
        )

        assertThat(
            schedulesStreamRepository.getStream()
                .take(1)
                .first(),
            equalTo(schedules)
        )
    }
}