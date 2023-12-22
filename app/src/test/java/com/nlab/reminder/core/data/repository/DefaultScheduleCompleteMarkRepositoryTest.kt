package com.nlab.reminder.core.data.repository

import com.nlab.reminder.core.data.model.ScheduleCompleteMark
import com.nlab.reminder.core.data.model.ScheduleCompleteMarkTable
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genBothify
import com.nlab.testkit.genInt
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
internal class DefaultScheduleCompleteMarkRepositoryTest {
    @Test
    fun `When inserted, Then updated ScheduleCompletedMarkTable`() = runTest {
        val txId = genBothify()
        val scheduleId = genScheduleId()
        val isComplete = genBoolean()
        val scheduleCompleteMarkRepository = genScheduleCompleteMarkRepository(
            createCompleteMark = mock {
                whenever(mock(isComplete)) doReturn ScheduleCompleteMark(isComplete, txId)
            }
        )

        scheduleCompleteMarkRepository.insert(scheduleId, isComplete)
        val snapshot = scheduleCompleteMarkRepository.get().value
        assertThat(
            snapshot.value.getValue(scheduleId),
            equalTo(ScheduleCompleteMark(isComplete, txId))
        )
    }

    @Test
    fun `When cleared, Then updated to empty table`() = runTest {
        val scheduleCompleteMarkRepository = genScheduleCompleteMarkRepository()
        (0..genInt(min = 1))
            .forEach { num -> scheduleCompleteMarkRepository.insert(genScheduleId(num.toLong()), genBoolean()) }
        scheduleCompleteMarkRepository.clear()

        val snapshot = scheduleCompleteMarkRepository.get().value
        assertThat(snapshot, equalTo(ScheduleCompleteMarkTable()))
    }
}

private fun genScheduleCompleteMarkRepository(
    createCompleteMark: (Boolean) -> ScheduleCompleteMark = mock()
): ScheduleCompleteMarkRepository = DefaultScheduleCompleteMarkRepository(createCompleteMark)