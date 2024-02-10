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
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.model.genTagUsageCount
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.statekit.middleware.interceptor.scenario
import com.nlab.testkit.genBothify
import com.nlab.testkit.once
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
        val tagRepository = mock<TagRepository> {
            whenever(mock.getUsageCount(target)) doReturn Result.Success(usageCount)
        }
        val loadedTagConfigMetadata: (HomeAction.TagConfigMetadataLoaded) -> Unit = mock()

        genHomeInterceptor(tagRepository = tagRepository)
            .scenario()
            .initState(genHomeUiStateSuccess())
            .action(HomeAction.OnTagLongClicked(tag = target))
            .hookIf<HomeAction.TagConfigMetadataLoaded> { loadedTagConfigMetadata(it) }
            .dispatchIn(testScope = this)
        verify(loadedTagConfigMetadata, once()).invoke(HomeAction.TagConfigMetadataLoaded(target, usageCount))
    }

    @Test(expected = IllegalStateException::class)
    fun `Sending error message, when tag was long clicked`() = runTest {
        val target = genTag()
        val error = IllegalStateException()
        val tagRepository = mock<TagRepository> {
            whenever(mock.getUsageCount(target)) doReturn Result.Failure(error)
        }
        val errorOccurred: (HomeAction.ErrorOccurred) -> Unit = mock()

        genHomeInterceptor(tagRepository = tagRepository)
            .scenario()
            .initState(genHomeUiStateSuccess())
            .action(HomeAction.OnTagLongClicked(tag = target))
            .hookIf<HomeAction.ErrorOccurred> { errorOccurred(it) }
            .dispatchIn(testScope = this)
        verify(errorOccurred, once()).invoke(HomeAction.ErrorOccurred(error))
    }

    @Test
    fun `update tag name, when tag rename confirmed`() = runTest {
        val tag = genTag()
        val updateName = genBothify("update name: ????")
        val tagRepository: TagRepository = mock {
            whenever(mock.updateName(tag, updateName)) doReturn Result.Success(Unit)
        }

        genHomeInterceptor(tagRepository = tagRepository)
            .scenario()
            .initState(genHomeUiStateSuccess(workflow = genHomeTagRenameWorkflow(tag = tag, renameText = updateName)))
            .action(HomeAction.OnTagRenameConfirmClicked)
            .dispatchIn(testScope = this)
        verify(tagRepository, once()).updateName(tag, updateName)
    }

    @Test(expected = IllegalStateException::class)
    fun `Failed rename tag, when renameTagConfig was not set`() = runTest {
        genHomeInterceptor()
            .scenario()
            .initState(
                genHomeUiStateSuccess(
                    workflow = genHomeWorkflowExcludeEmpty(ignoreCases = setOf(HomeWorkflow.TagRename::class))
                )
            )
            .action(HomeAction.OnTagRenameConfirmClicked)
            .dispatchIn(testScope = this)
    }

    @Test
    fun `Delete tag, when tag delete confirmed`() = runTest {
        val tagDelete = genHomeTagDeleteConfig()
        val tagRepository: TagRepository = mock {
            whenever(mock.delete(tagDelete.tag)) doReturn Result.Success(Unit)
        }

        genHomeInterceptor(tagRepository = tagRepository)
            .scenario()
            .initState(genHomeUiStateSuccess(workflow = tagDelete))
            .action(HomeAction.OnTagDeleteConfirmClicked)
            .dispatchIn(testScope = this)
        verify(tagRepository, once()).delete(tagDelete.tag)
    }

    @Test(expected = IllegalStateException::class)
    fun `Failed delete tag, when deleteTagConfig was not set`() = runTest {
        genHomeInterceptor()
            .scenario()
            .initState(
                genHomeUiStateSuccess(
                    workflow = genHomeWorkflowExcludeEmpty(ignoreCases = setOf(HomeWorkflow.TagDelete::class))
                )
            )
            .action(HomeAction.OnTagDeleteConfirmClicked)
            .dispatchIn(testScope = this)
    }
}

private fun genHomeInterceptor(tagRepository: TagRepository = mock()) = HomeInterceptor(
    tagRepository = tagRepository
)