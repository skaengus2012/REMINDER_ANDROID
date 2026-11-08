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
<<<<<<< HEAD:core/data-impl/src/test/kotlin/com/nlab/reminder/core/data/repository/impl/DefaultCompletedScheduleShownRepositoryTest.kt
internal class DefaultCompletedScheduleShownRepositoryTest {
=======
class CompletedScheduleDetailShownRepositoryImplTest {
>>>>>>> master:core/data-impl/src/test/kotlin/com/nlab/reminder/core/data/repository/impl/CompletedScheduleDetailShownRepositoryImplTest.kt
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
<<<<<<< HEAD:core/data-impl/src/test/kotlin/com/nlab/reminder/core/data/repository/impl/DefaultCompletedScheduleShownRepositoryTest.kt
    getAsStreamFunction: () -> Flow<Boolean> = mockk(relaxed = true),
    setShownFunction: suspend (isShown: Boolean) -> Result<Unit> = mockk(relaxed = true)
) = DefaultCompletedScheduleShownRepository(getAsStreamFunction, setShownFunction)
=======
    getAsStreamFunc: () -> Flow<Boolean> = mockk(relaxed = true),
    setShownFunc: suspend (isShown: Boolean) -> Result<Unit> = { Result.success(Unit) }
) = CompletedScheduleShownRepositoryImpl(getAsStreamFunc, setShownFunc)
>>>>>>> master:core/data-impl/src/test/kotlin/com/nlab/reminder/core/data/repository/impl/CompletedScheduleDetailShownRepositoryImplTest.kt
