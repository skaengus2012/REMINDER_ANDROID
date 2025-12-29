package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.kotlin.Result
import com.nlab.testkit.faker.genBoolean
import io.mockk.coVerify
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
internal class DefaultCompletedScheduleShownRepositoryTest {
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
        val setShownFunction: suspend (isShown: Boolean) -> Result<Unit> = mockk(relaxed = true)
        val repository = genCompletedScheduleShownRepository(setShownFunction = setShownFunction)
        val input = genBoolean()

        repository.setShown(input)
        coVerify(exactly = 1) {
            setShownFunction.invoke(input)
        }
    }
}

private fun genCompletedScheduleShownRepository(
    getAsStreamFunction: () -> Flow<Boolean> = mockk(relaxed = true),
    setShownFunction: suspend (isShown: Boolean) -> Result<Unit> = mockk(relaxed = true)
) = DefaultCompletedScheduleShownRepository(getAsStreamFunction, setShownFunction)