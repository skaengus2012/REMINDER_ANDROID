package com.nlab.statekit.plugins.extension

import com.nlab.statekit.plugins.StatekitPlugin
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class StoreMaterialScopeKtTest {
    @After
    fun tearDown() {
        StatekitPlugin.setGlobalExceptionHandler(null)
    }

    @Test(expected = TestException::class)
    fun `When occurred exception without globalException, Then throw exception`() = runTest {
        CoroutineScope(Dispatchers.Unconfined).toStoreMaterialScope()
            .launch { throw TestException() }
            .join()
    }

    @Test
    fun `Given globalExceptionHandler, When occurred exception, Then handled globalExceptionHandler`() = runTest {
        val exception = TestException()
        val onError: (Throwable) -> Unit = mock()
        StatekitPlugin.setGlobalExceptionHandler(CoroutineExceptionHandler { _, throwable -> onError(throwable) })
        CoroutineScope(Dispatchers.Unconfined).toStoreMaterialScope()
            .launch { throw exception }
            .join()

        verify(onError, once()).invoke(exception)
    }

    @Test
    fun `Given local and global ExceptionHandler, When occurred exception, Then handled to all`() = runTest {
        val exception = TestException()
        val localExceptionHandler: (Throwable) -> Unit = mock()
        val globalExceptionHandler: (Throwable) -> Unit = mock()
        StatekitPlugin.setGlobalExceptionHandler(CoroutineExceptionHandler { _, throwable ->
            globalExceptionHandler(throwable)
        })
        val localCoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            localExceptionHandler(throwable)
        }
        CoroutineScope(Dispatchers.Unconfined + localCoroutineExceptionHandler).toStoreMaterialScope()
            .launch { throw exception }
            .join()

        inOrder(localExceptionHandler, globalExceptionHandler) {
            verify(localExceptionHandler, once()).invoke(exception)
            verify(globalExceptionHandler, once()).invoke(exception)
        }
    }
}

private class TestException : RuntimeException()