package com.nlab.reminder.core.domain

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.repository.ScheduleCompleteMarkRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.ScheduleUpdateRequest
import com.nlab.reminder.core.kotlin.coroutine.Delay
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genInt
import com.nlab.testkit.once
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
class UpdateScheduleCompletionWithMarkUseCaseTest {

    @Test
    fun `When invoked, Then completeMark add`() = runTest {
        val scheduleId = genScheduleId()
        val isComplete = false // When flag put a value other than false, Jacoco coverage did not work normally. 😭
        val completeMarkRepository = genCompleteMarkRepositoryMockWithEmptyTable()
        val useCase = genUpdateScheduleCompletionUseCase(
            completeMarkRepository = completeMarkRepository
        )

        useCase.invoke(scheduleId, isComplete)
        verify(completeMarkRepository, once()).add(scheduleId, isComplete)
    }

    @Test
    fun `Given completed mark added, Then scheduleRepository update complete from completeMark tables`() = runTest {
        val expectedCompleteTable: ImmutableMap<ScheduleId, Boolean> =
            List(genInt(min = 1, max = 10)) { index -> genScheduleId(index.toLong()) }
                .associateWith { true }
                .toPersistentMap()
        val scheduleRepository: ScheduleRepository = mock()
        val completeMarkRepository: ScheduleCompleteMarkRepository = mock {
            whenever(mock.get()) doReturn MutableStateFlow(
                expectedCompleteTable
            )
        }
        val useCase = genUpdateScheduleCompletionUseCase(
            scheduleRepository = scheduleRepository,
            completeMarkRepository = completeMarkRepository
        )

        useCase.invoke(genScheduleId(), genBoolean())
        verify(scheduleRepository, once()).update(ScheduleUpdateRequest.Completes(expectedCompleteTable))
    }
    
    @Test  
    fun `Given completed mark not existed, Then scheduleRepository never updated`() = runTest {
        val scheduleRepository: ScheduleRepository = mock()
        val useCase = genUpdateScheduleCompletionUseCase(
            completeMarkRepository = genCompleteMarkRepositoryMockWithEmptyTable(),
            scheduleRepository = scheduleRepository
        )

        useCase.invoke(genScheduleId(), genBoolean())
        verify(scheduleRepository, never()).update(any())
    }

    @Test
    fun `AggregateDelay called between adding completeMark and updating scheduleRepository`() = runTest {
        val completeMarkRepository: ScheduleCompleteMarkRepository = mock {
            whenever(mock.get()) doReturn MutableStateFlow(persistentMapOf(genScheduleId() to genBoolean()))
        }
        val aggregateDelay: Delay = mock()
        val scheduleRepository: ScheduleRepository = mock()
        val useCase = genUpdateScheduleCompletionUseCase(
            scheduleRepository = scheduleRepository,
            completeMarkRepository = completeMarkRepository,
            aggregateDelay = aggregateDelay
        )
        val scheduleId = genScheduleId()
        val isComplete = genBoolean()

        useCase.invoke(scheduleId,isComplete)
        inOrder(scheduleRepository, completeMarkRepository, aggregateDelay) {
            verify(completeMarkRepository).add(scheduleId, isComplete)
            verify(aggregateDelay).invoke()
            verify(scheduleRepository).update(any())
        }
    }
}

private fun genUpdateScheduleCompletionUseCase(
    scheduleRepository: ScheduleRepository = mock(),
    completeMarkRepository: ScheduleCompleteMarkRepository = mock(),
    aggregateDelay: Delay = mock()
): UpdateScheduleCompletionWithMarkUseCase = UpdateScheduleCompletionWithMarkUseCase(
    scheduleRepository = scheduleRepository,
    completeMarkRepository = completeMarkRepository,
    aggregateDelay = aggregateDelay
)

private fun genCompleteMarkRepositoryMockWithEmptyTable(): ScheduleCompleteMarkRepository = mock {
    whenever(mock.get()) doReturn MutableStateFlow(persistentMapOf())
}