package com.nlab.reminder.core.data.repository.impl

import com.nlab.testkit.faker.genBoolean
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class CompletedScheduleDetailShownRepositoryImplTest {
    @Test
    fun `When get as stream, Then getAsStreamFunction published`() = runTest {
        val expectedComplete = genBoolean()
        val repository = genCompletedScheduleShownRepository(
            getAsStreamFunc = { flowOf(expectedComplete) }
        )
        assertThat(
            expectedComplete,
            equalTo(repository.getAsStream().first())
        )
    }

    @Test
    fun `When set shown, Then setShownFunc invoked once with input`() = runTest {
        suspend fun verify(expectedInput: Boolean) {
            val invokedInputs = mutableListOf<Boolean>()
            val expectedResult = Result.success(Unit)
            val repository = genCompletedScheduleShownRepository(
                setShownFunc = suspend { isShown ->
                    invokedInputs += isShown
                    expectedResult
                }
            )
            val actualResult = repository.setShown(expectedInput)
            assertThat(invokedInputs, equalTo(listOf(expectedInput)))
            assertThat(actualResult, equalTo(expectedResult))
        }

        verify(true)
        verify(false)
    }
}

private fun genCompletedScheduleShownRepository(
    getAsStreamFunc: () -> Flow<Boolean> = mockk(relaxed = true),
    setShownFunc: suspend (isShown: Boolean) -> Result<Unit> = { Result.success(Unit) }
) = CompletedScheduleShownRepositoryImpl(getAsStreamFunc, setShownFunc)