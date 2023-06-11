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
import com.nlab.reminder.domain.common.data.model.genTag
import com.nlab.reminder.domain.common.data.model.genTagUsageCount
import com.nlab.reminder.domain.common.data.repository.TagRepository
import com.nlab.reminder.test.unconfinedCoroutineScope
import com.nlab.statekit.middleware.interceptor.Interceptor
import com.nlab.statekit.util.buildInterceptor
import com.nlab.statekit.util.createStore
import com.nlab.statekit.util.*
import com.nlab.testkit.ExpectedRuntimeException
import com.nlab.testkit.genBothify
import com.nlab.testkit.once
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
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
        val usageCount = genTagUsageCount()
        val tagRepository: TagRepository = mock {
            whenever(mock.getUsageCount(target)) doReturn Result.Success(usageCount)
        }
        val loadedTagConfigMetadata: (HomeAction.TagConfigMetadataLoaded) -> Unit = mock()
        dispatchWithInterceptor(
            action = HomeAction.OnTagLongClicked(tag = target),
            interceptor = genHomeInterceptor(tagRepository),
            additionalInterceptors = listOf(
                buildInterceptor {
                    loadedTagConfigMetadata(
                        it.action as? HomeAction.TagConfigMetadataLoaded ?: return@buildInterceptor
                    )
                }
            )
        )
        verify(loadedTagConfigMetadata, once()).invoke(HomeAction.TagConfigMetadataLoaded(target, usageCount))
    }

    @Test(expected = ExpectedRuntimeException::class)
    fun `Sending error message, when tag was long clicked`() = runTest {
        val target = genTag()
        val error = ExpectedRuntimeException()
        val tagRepository: TagRepository = mock {
            whenever(mock.getUsageCount(target)) doReturn Result.Failure(error)
        }
        val errorOccurred: (HomeAction.ErrorOccurred) -> Unit = mock()
        dispatchWithInterceptor(
            action = HomeAction.OnTagLongClicked(tag = target),
            interceptor = genHomeInterceptor(tagRepository),
            additionalInterceptors = listOf(
                buildInterceptor {
                    errorOccurred(
                        it.action as? HomeAction.ErrorOccurred ?: return@buildInterceptor
                    )
                }
            )
        )
        verify(errorOccurred, once()).invoke(HomeAction.ErrorOccurred(error))
    }

    @Test
    fun `update tag name, when tag rename confirmed`() = runTest {
        val tag = genTag()
        val updateName = genBothify("update name: ????")
        val tagRepository: TagRepository = mock {
            whenever(mock.updateName(tag, updateName)) doReturn Result.Success(Unit)
        }
        dispatchWithInterceptor(
            initState = genHomeUiStateSuccess(
                tagRenameTarget = genTagRenameConfig(
                    tag = tag,
                    renameText = updateName
                )
            ),
            action = HomeAction.OnTagRenameConfirmClicked,
            interceptor = genHomeInterceptor(tagRepository = tagRepository),
        )
        verify(tagRepository, once()).updateName(tag, updateName)
    }

    @Test(expected = IllegalStateException::class)
    fun `Failed rename tag, when renameTagConfig was not set`() = runTest {
        dispatchWithInterceptor(
            initState = genHomeUiStateSuccess(tagRenameTarget = null),
            action = HomeAction.OnTagRenameConfirmClicked,
        )
    }
}

private suspend fun TestScope.dispatchWithInterceptor(
    action: HomeAction,
    initState: HomeUiState = genHomeUiStateSuccess(),
    interceptor: HomeInterceptor = genHomeInterceptor(),
    additionalInterceptors: List<Interceptor<HomeAction, HomeUiState>> = emptyList()
) {
    var concatInterceptors: Interceptor<HomeAction, HomeUiState> = interceptor
    additionalInterceptors.forEach { concatInterceptors += it }
    val store = createStore(
        unconfinedCoroutineScope(),
        initState = initState,
        interceptor = concatInterceptors
    )
    store.dispatch(action).join()
}

private fun genHomeInterceptor(
    tagRepository: TagRepository = mock()
) = HomeInterceptor(
    tagRepository = tagRepository
)