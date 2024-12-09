/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.tag.edit

import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.data.repository.SaveTagQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.domain.TagGroupSource
import com.nlab.reminder.core.domain.TryUpdateTagNameResult
import com.nlab.reminder.core.domain.TryUpdateTagNameUseCase
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.reminder.core.kotlin.isFailure
import com.nlab.reminder.core.kotlin.isSuccess
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.testkit.faker.genBothify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.test.assertFlowEmissionsLazy
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
class TagEditDelegateTest {
    @Test
    fun `Given initState, When create, Then delegate has initState`() {
        val initState = genTagEditState()
        val delegate = genTagEditDelegate(initState)
        val actualState = delegate.state.value
        assertThat(actualState, equalTo(initState))
    }

    @Test
    fun `Given empty state, tag and success of getUsageCount, When startEditing, Then return success and changed state to Intro`() = runTest {
        val initState = TagEditState.Empty
        val tag = genTag()
        val usageCount = genNonNegativeLong()
        val delegate = genTagEditDelegate(
            initState,
            tagRepository = mock {
                whenever(mock.getUsageCount(id = tag.id)) doReturn Result.Success(usageCount)
            }
        )
        val expectedState = TagEditState.Intro(
            tag = tag,
            usageCount = usageCount
        )

        val actualResult = delegate.startEditing(tag)
        assertThat(actualResult.isSuccess, equalTo(true))
        assertThat(delegate.state.value, equalTo(expectedState))
    }

    @Test
    fun `Given not empty state and success of getUsageCount, When startEditing, Then return success and not changed state`() = runTest {
        val initState = genTagEditStateExcludeTypeOf<TagEditState.Empty>()
        val tag = genTag()
        val delegate = genTagEditDelegate(
            initState,
            tagRepository = mock {
                whenever(mock.getUsageCount(id = tag.id)) doReturn Result.Success(genNonNegativeLong())
            }
        )

        val actualResult = delegate.startEditing(tag)
        assertThat(actualResult.isSuccess, equalTo(true))
        assertThat(delegate.state.value, equalTo(initState))
    }

    @Test
    fun `Given tag and fail of getUsageCount, When startEditing, Then return failure`() = runTest {
        val tag = genTag()
        val delegate = genTagEditDelegate(
            genTagEditState(),
            tagRepository = mock {
                whenever(mock.getUsageCount(id = tag.id)) doReturn Result.Failure(IllegalStateException())
            }
        )

        val actualResult = delegate.startEditing(tag)
        assertThat(actualResult.isFailure, equalTo(true))
    }

    @Test
    fun `Given intro state, When start rename, Then changed state to rename`() {
        val initState = genIntroState()
        val delegate = genTagEditDelegate(initState = initState)
        val expectedState = TagEditState.Rename(
            tag = initState.tag,
            usageCount = initState.usageCount,
            renameText = initState.tag.name.value,
            shouldUserInputReady = true,
        )

        delegate.startRename()
        assertThat(delegate.state.value, equalTo(expectedState))
    }

    @Test
    fun `Given not intro state, When start rename, Then not changed state`() {
        val initState = genTagEditStateExcludeTypeOf<TagEditState.Intro>()
        val delegate = genTagEditDelegate(initState = initState)

        delegate.startRename()
        assertThat(delegate.state.value, equalTo(initState))
    }

    @Test
    fun `Given rename state, When ready rename input, Then changed shouldUserInputReady to false`() {
        val initState = genRenameState(shouldUserInputReady = true)
        val delegate = genTagEditDelegate(initState = initState)
        val expectedState = initState.copy(shouldUserInputReady = false)

        delegate.readyRenameInput()
        assertThat(delegate.state.value, equalTo(expectedState))
    }

    @Test
    fun `Given rename state and inputText, When change rename text, Then changed rename to inputText`() {
        val initState = genRenameState(renameText = "")
        val inputText = genBothify()
        val delegate = genTagEditDelegate(initState = initState)

        val expectedState = initState.copy(renameText = inputText)
        delegate.changeRenameText(inputText)
        assertThat(delegate.state.value, equalTo(expectedState))
    }

    @Test
    fun `Given not rename state, When try update tag name, Then return success and not changed state`() = runTest {
        val initState = genTagEditStateExcludeTypeOf<TagEditState.Rename>()
        val delegate = genTagEditDelegate(initState = initState)

        val result = delegate.tryUpdateTagName(loadedTagsSnapshot = genTags())
        assertThat(result.isSuccess, equalTo(true))
        assertThat(delegate.state.value, equalTo(initState))
    }

    @Test
    fun `Given empty input rename state, When try update tag name, Then return success and changed state to empty`() = runTest {
        val initState = genRenameState(renameText = "")
        val delegate = genTagEditDelegate(initState = initState)

        val result = delegate.tryUpdateTagName(loadedTagsSnapshot = genTags())
        assertThat(result.isSuccess, equalTo(true))
        assertThat(delegate.state.value, equalTo(TagEditState.Empty))
    }
    
    @Test
    fun `Given rename state, tags and result of tryUpdateTagNameUseCase is Success or NotChanged, When try update tag name, Then return success and changed state to processing rename, empty`() = runTest {
        suspend fun testTryUpdateTagName(tryUpdateTagNameResult: TryUpdateTagNameResult) {
            val initState = genRenameState()
            val tags = genTags()
            val delegate = genTagEditDelegate(
                initState = initState,
                tryUpdateTagNameUseCase = mock {
                    whenever(
                        mock.invoke(
                            tagId = initState.tag.id,
                            newName = initState.renameText.toNonBlankString(),
                            tagGroup = TagGroupSource.Snapshot(tags)
                        )
                    ).doSuspendableAnswer {
                        delay(1_000) // for prevent conflate.
                        tryUpdateTagNameResult
                    }
                }
            )
            val assertStateChanged = assertFlowEmissionsLazy(
                flow = delegate.state.drop(1),
                expectedEmits = listOf(
                    TagEditState.Processing(initState),
                    TagEditState.Empty
                )
            )

            val result = delegate.tryUpdateTagName(loadedTagsSnapshot = tags)
            assertThat(result.isSuccess, equalTo(true))
            assertStateChanged()
        }

        testTryUpdateTagName(TryUpdateTagNameResult.Success(genTag()))
        testTryUpdateTagName(TryUpdateTagNameResult.NotChanged)
    }

    @Test
    fun `Given rename state, tags and result of tryUpdateTagNameUseCase is DuplicateNameError, When try update tag name, Then return success and changed state to processing, merge`() = runTest {
        val targetTag = genTag()
        val duplicateTag = genTag()
        val initState = genRenameState(tag = targetTag)
        val tags = genTags()
        val delegate = genTagEditDelegate(
            initState = initState,
            tryUpdateTagNameUseCase = mock {
                whenever(
                    mock.invoke(
                        tagId = initState.tag.id,
                        newName = initState.renameText.toNonBlankString(),
                        tagGroup = TagGroupSource.Snapshot(tags)
                    )
                ).doSuspendableAnswer {
                    delay(1_000) // for prevent conflate.
                    TryUpdateTagNameResult.DuplicateNameError(duplicateTag)
                }
            }
        )
        val assertStateChanged = assertFlowEmissionsLazy(
            flow = delegate.state.drop(1),
            expectedEmits = listOf(
                TagEditState.Processing(initState),
                TagEditState.Merge(from = targetTag, to = duplicateTag)
            )
        )
        val result = delegate.tryUpdateTagName(tags)
        assertThat(result.isSuccess, equalTo(true))
        assertStateChanged()
    }

    @Test
    fun `Given rename state, tags and result of tryUpdateTagNameUseCase is UnknownError, When try update tag name, Then return fail and changed state to processing, empty`() = runTest {
        val initState = genRenameState()
        val tags = genTags()
        val delegate = genTagEditDelegate(
            initState = initState,
            tryUpdateTagNameUseCase = mock {
                whenever(
                    mock.invoke(
                        tagId = initState.tag.id,
                        newName = initState.renameText.toNonBlankString(),
                        tagGroup = TagGroupSource.Snapshot(tags)
                    )
                ).doReturn(TryUpdateTagNameResult.UnknownError)
            }
        )
        val assertStateChanged = assertFlowEmissionsLazy(
            flow = delegate.state.drop(1),
            expectedEmits = listOf(
                TagEditState.Processing(initState),
                TagEditState.Empty
            )
        )
        val result = delegate.tryUpdateTagName(tags)
        assertThat(result.isFailure, equalTo(true))
        assertStateChanged()
    }

    @Test
    fun `Given not merge state, When merge tag, Then return success and not changed state`() = runTest {
        val initState = genTagEditStateExcludeTypeOf<TagEditState.Merge>()
        val delegate = genTagEditDelegate(initState = initState)

        val result = delegate.mergeTag()
        assertThat(result.isSuccess, equalTo(true))
        assertThat(delegate.state.value, equalTo(initState))
    }


    @Test
    fun `Given merge tag and success of save, When merge tag, Then return success and changed state to processing, empty`() = runTest {
        val initState = genMergeState()
        val delegate = genTagEditDelegate(
            initState = initState,
            tagRepository = mock {
                whenever(mock.save(query = SaveTagQuery.Modify(id = initState.from.id, name = initState.to.name)))
                    .doReturn(Result.Success(genTag()))
            }
        )
        val assertStateChanged = assertFlowEmissionsLazy(
            flow = delegate.state.drop(1),
            expectedEmits = listOf(
                TagEditState.Processing(initState),
                TagEditState.Empty
            )
        )
        val result = delegate.mergeTag()
        assertThat(result.isSuccess, equalTo(true))
        assertStateChanged()
    }

    @Test
    fun `Given merge tag and fail of save, When merge tag, Then return fail and changed state to processing, empty`() = runTest {
        val initState = genMergeState()
        val delegate = genTagEditDelegate(
            initState = initState,
            tagRepository = mock {
                whenever(mock.save(query = SaveTagQuery.Modify(id = initState.from.id, name = initState.to.name)))
                    .doReturn(Result.Failure(IllegalStateException()))
            }
        )
        val assertStateChanged = assertFlowEmissionsLazy(
            flow = delegate.state.drop(1),
            expectedEmits = listOf(
                TagEditState.Processing(initState),
                TagEditState.Empty
            )
        )
        val result = delegate.mergeTag()
        assertThat(result.isFailure, equalTo(true))
        assertStateChanged()
    }

    @Test
    fun `Given intro tag, When start delete, Then changed state to delete`() {
        val state = genIntroState()
        val delegate = genTagEditDelegate(initState = state)
        delegate.startDelete()

        val expectedState = TagEditState.Delete(
            tag = state.tag,
            usageCount = state.usageCount
        )
        assertThat(delegate.state.value, equalTo(expectedState))
    }

    @Test
    fun `Given not delete state, When delete tag, Then return success and not changed state`() = runTest {
        val initState = genTagEditStateExcludeTypeOf<TagEditState.Delete>()
        val delegate = genTagEditDelegate(initState = initState)

        val result = delegate.deleteTag()
        assertThat(result.isSuccess, equalTo(true))
        assertThat(delegate.state.value, equalTo(initState))
    }

    @Test
    fun `Given delete state and success of delete, When delete tag, Then return success and changed state to processing, empty`() = runTest {
        val initState = genDeleteState()
        val delegate = genTagEditDelegate(
            initState = initState,
            tagRepository = mock {
                whenever(mock.delete(initState.tag.id)) doReturn Result.Success(Unit)
            }
        )
        val assertStateChanged = assertFlowEmissionsLazy(
            flow = delegate.state.drop(1),
            expectedEmits = listOf(
                TagEditState.Processing(initState),
                TagEditState.Empty
            )
        )
        val result = delegate.deleteTag()
        assertThat(result.isSuccess, equalTo(true))
        assertStateChanged()
    }

    @Test
    fun `Given delete state and fail of delete, When delete tag, Then return fail and changed state to empty`() = runTest {
        val initState = genDeleteState()
        val delegate = genTagEditDelegate(
            initState = initState,
            tagRepository = mock {
                whenever(mock.delete(initState.tag.id)) doReturn Result.Failure(IllegalStateException())
            }
        )
        val assertStateChanged = assertFlowEmissionsLazy(
            flow = delegate.state.drop(1),
            expectedEmits = listOf(
                TagEditState.Processing(initState),
                TagEditState.Empty
            )
        )
        val result = delegate.deleteTag()
        assertThat(result.isFailure, equalTo(true))
        assertStateChanged()
    }

    @Test
    fun `Given not empty state, When clear state, Then change state to empty`() {
        val initState = genTagEditState()
        val delegate = genTagEditDelegate(initState = initState)
        delegate.clearState()

        assertThat(delegate.state.value, equalTo(TagEditState.Empty))
    }
}

private fun genTagEditDelegate(
    initState: TagEditState,
    tagRepository: TagRepository = mock(),
    tryUpdateTagNameUseCase: TryUpdateTagNameUseCase = mock(),
): TagEditDelegate = TagEditDelegate(initState, tagRepository, tryUpdateTagNameUseCase)