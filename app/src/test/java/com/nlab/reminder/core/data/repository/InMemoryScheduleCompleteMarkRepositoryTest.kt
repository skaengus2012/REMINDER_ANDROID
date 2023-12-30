package com.nlab.reminder.core.data.repository

import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genInt
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
internal class InMemoryScheduleCompleteMarkRepositoryTest {
    @Test
    fun `When added, Then updated completeMark table`() = runTest {
        val scheduleId = genScheduleId()
        val isComplete = genBoolean()
        val repository = InMemoryScheduleCompleteMarkRepository()

        repository.add(scheduleId, isComplete)
        assertThat(
            repository.getSnapshot().getValue(scheduleId),
            equalTo(isComplete)
        )
    }

    @Test
    fun `Given already added completeMark, When same scheduleId completion added, Then old completeMark override`() = runTest {
        val scheduleId = genScheduleId()
        val oldCompleteMark = genBoolean()
        val newCompleteMark = oldCompleteMark.not()
        val scheduleCompleteMarkRepository = InMemoryScheduleCompleteMarkRepository().apply {
            add(scheduleId, oldCompleteMark)
        }

        scheduleCompleteMarkRepository.add(scheduleId, newCompleteMark)
        assertThat(
            scheduleCompleteMarkRepository.getSnapshot(),
            equalTo(persistentMapOf(scheduleId to newCompleteMark))
        )
    }

    @Test
    fun `When cleared, Then updated to empty table`() = runTest {
        val scheduleCompleteMarkRepository = InMemoryScheduleCompleteMarkRepository().apply {
            (0..genInt(min = 1, max = 10)).forEach { add(genScheduleId(it.toLong()), genBoolean()) }
        }

        scheduleCompleteMarkRepository.clear()
        assertThat(
            scheduleCompleteMarkRepository.getSnapshot(),
            equalTo(persistentMapOf())
        )
    }
}