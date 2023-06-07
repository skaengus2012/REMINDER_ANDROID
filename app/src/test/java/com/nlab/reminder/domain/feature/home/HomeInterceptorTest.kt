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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.domain.common.data.model.TagUsageCount
import com.nlab.reminder.domain.common.data.model.genTag
import com.nlab.reminder.domain.common.data.repository.TagRepository
import com.nlab.reminder.test.unconfinedCoroutineScope
import com.nlab.statekit.util.buildInterceptor
import com.nlab.statekit.util.createStore
import com.nlab.statekit.util.*
import com.nlab.testkit.genBothify
import com.nlab.testkit.genLong
import com.nlab.testkit.once
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.plus
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
internal class HomeInterceptorTest {
    @Test
    fun `Sending TagConfigMetadata, when tag was long clicked`() = runTest {
        val target = genTag()
        val usageCount = TagUsageCount(genLong(min = 10, max = 100))
        val tagRepository: TagRepository = mock {
            whenever(mock.getUsageCount(target)) doReturn Result.Success(usageCount)
        }
        val loadedTagConfigMetadata: (HomeAction.TagConfigMetadataLoaded) -> Unit = mock()
        val store = createStore(
            unconfinedCoroutineScope(),
            initState = genHomeUiStateSuccess(),
            interceptor = genHomeInterceptor(tagRepository = tagRepository) + buildInterceptor {
                loadedTagConfigMetadata(
                    it.action as? HomeAction.TagConfigMetadataLoaded ?: return@buildInterceptor
                )
            }
        )
        store.dispatch(HomeAction.OnTagLongClicked(tag = target)).join()
        verify(loadedTagConfigMetadata, once()).invoke(
            HomeAction.TagConfigMetadataLoaded(target, usageCount.value)
        )
    }

    @Test
    fun `Sending error message, when tag was long clicked`() = runTest {
        val target = genTag()
        val error = RuntimeException(genBothify("error occurred: ????"))
        val tagRepository: TagRepository = mock {
            whenever(mock.getUsageCount(target)) doReturn Result.Failure(error)
        }
        val exceptionDeferred = CompletableDeferred<Throwable>()
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            exceptionDeferred.complete(throwable)
        }
        val errorOccurred: (HomeAction.ErrorOccurred) -> Unit = mock()
        val store = createStore(
            coroutineScope = unconfinedCoroutineScope() + exceptionHandler,
            initState = genHomeUiStateSuccess(),
            interceptor = genHomeInterceptor(tagRepository = tagRepository) + buildInterceptor {
                errorOccurred(
                    it.action as? HomeAction.ErrorOccurred ?: return@buildInterceptor
                )
            }
        )
        store.dispatch(HomeAction.OnTagLongClicked(tag = target)).join()
        verify(errorOccurred, once()).invoke(HomeAction.ErrorOccurred(error))
        assertThat(
            exceptionDeferred.await().message,
            equalTo(error.message)
        )
    }
}

private fun genHomeInterceptor(
    tagRepository: TagRepository = mock()
) = HomeInterceptor(
    tagRepository = tagRepository
)