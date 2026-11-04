package com.nlab.reminder.core.data.repository.impl

import com.nlab.testkit.faker.genBoolean
import io.mockk.mockk
import kotlinx.coroutines.delay
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
internal class CompletedScheduleDetailShownRepositoryImplTest {
    @Test
    fun `When get as stream, Then getAsStreamFunction published`() = runTest {
        val expectedComplete = genBoolean()
        val repository = genCompletedScheduleShownRepository(
            getAsStreamFunction = { flowOf(expectedComplete) }
        )
        assertThat(
            expectedComplete,
            equalTo(repository.getAsStream().first())
        )
    }

    @Test
    fun `When set shown, Then setShownFunction invoked`() = runTest {
        // mockk can't stub suspend lambda returning value class (kotlin.Result), so use a manual fake
        val invokedInputs = mutableListOf<Boolean>()
        val expectedResult = Result.success(Unit)
        val repository = genCompletedScheduleShownRepository(
            setShownFunction = { isShown ->
                invokedInputs += isShown
                expectedResult
            }
        )
        val input = genBoolean()

        val actualResult = repository.setShown(input)
        assertThat(invokedInputs, equalTo(listOf(input)))
        assertThat(actualResult, equalTo(expectedResult))
    }

    @Test
    fun `Given suspending setShownFunction, When set shown, Then return function result after resumed`() = runTest {
        // Verifies the result is propagated when the function actually suspends
        val expectedResult = Result.success(Unit)
        val repository = genCompletedScheduleShownRepository(
            setShownFunction = {
                delay(1)
                expectedResult
            }
        )

        assertThat(repository.setShown(genBoolean()), equalTo(expectedResult))
    }
}

private fun genCompletedScheduleShownRepository(
    getAsStreamFunction: () -> Flow<Boolean> = mockk(relaxed = true),
    setShownFunction: suspend (isShown: Boolean) -> Result<Unit> = { Result.success(Unit) }
) = CompletedScheduleShownRepositoryImpl(getAsStreamFunction, setShownFunction)