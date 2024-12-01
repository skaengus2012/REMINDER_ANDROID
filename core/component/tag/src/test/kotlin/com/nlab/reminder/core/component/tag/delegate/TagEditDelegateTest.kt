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

package com.nlab.reminder.core.component.tag.delegate

import com.nlab.reminder.core.component.tag.edit.TagEditDelegate
import com.nlab.reminder.core.component.tag.edit.TagEditStep
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.data.repository.SaveTagQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.domain.TagGroupSource
import com.nlab.reminder.core.domain.TryUpdateTagNameResult
import com.nlab.reminder.core.domain.TryUpdateTagNameUseCase
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.reminder.core.kotlin.isFailure
import com.nlab.reminder.core.kotlin.isSuccess
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.shuffleAndGetFirst
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedBackgroundScope
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
    fun `Given initStep, When create, Then delegate has initStep`() {
        val initStep = genTagEditStep()
        val delegate = genTagEditDelegate(initStep)
        val actualStep = delegate.step.value
        assertThat(actualStep, equalTo(initStep))
    }

    @Test
    fun `Given empty step, tag and success of getUsageCount, When startEditing, Then return success and changed step to Intro`() = runTest {
        val initStep = TagEditStep.Empty
        val tag = genTag()
        val usageCount = genNonNegativeLong()
        val delegate = genTagEditDelegate(
            initStep,
            tagRepository = mock {
                whenever(mock.getUsageCount(id = tag.id)) doReturn Result.Success(usageCount)
            }
        )
        val expectedStep = TagEditStep.Intro(
            tag = tag,
            usageCount = usageCount
        )

        val actualResult = delegate.startEditing(tag)
        assertThat(actualResult.isSuccess, equalTo(true))
        assertThat(delegate.step.value, equalTo(expectedStep))
    }

    @Test
    fun `Given not empty step and success of getUsageCount, When startEditing, Then return success and not changed step`() = runTest {
        val initStep = genTagEditStepWithoutTypeOf<TagEditStep.Empty>()
        val tag = genTag()
        val delegate = genTagEditDelegate(
            initStep,
            tagRepository = mock {
                whenever(mock.getUsageCount(id = tag.id)) doReturn Result.Success(genNonNegativeLong())
            }
        )

        val actualResult = delegate.startEditing(tag)
        assertThat(actualResult.isSuccess, equalTo(true))
        assertThat(delegate.step.value, equalTo(initStep))
    }

    @Test
    fun `Given tag and fail of getUsageCount, When startEditing, Then return failure`() = runTest {
        val tag = genTag()
        val delegate = genTagEditDelegate(
            genTagEditStep(),
            tagRepository = mock {
                whenever(mock.getUsageCount(id = tag.id)) doReturn Result.Failure(IllegalStateException())
            }
        )

        val actualResult = delegate.startEditing(tag)
        assertThat(actualResult.isFailure, equalTo(true))
    }

    @Test
    fun `Given intro step, When start rename, Then changed step to rename`() {
        val initStep = genIntroStep()
        val delegate = genTagEditDelegate(initStep = initStep)
        val expectedStep = TagEditStep.Rename(
            tag = initStep.tag,
            usageCount = initStep.usageCount,
            renameText = initStep.tag.name.value,
            shouldUserInputReady = true
        )

        delegate.startRename()
        assertThat(delegate.step.value, equalTo(expectedStep))
    }

    @Test
    fun `Given not intro step, When start rename, Then not changed step`() {
        val initStep = genTagEditStepWithoutTypeOf<TagEditStep.Intro>()
        val delegate = genTagEditDelegate(initStep = initStep)

        delegate.startRename()
        assertThat(delegate.step.value, equalTo(initStep))
    }

    @Test
    fun `Given rename step, When ready rename input, Then changed shouldUserInputReady to false`() {
        val initStep = genRenameStep(shouldUserInputReady = true)
        val delegate = genTagEditDelegate(initStep = initStep)
        val expectedStep = initStep.copy(shouldUserInputReady = false)

        delegate.readyRenameInput()
        assertThat(delegate.step.value, equalTo(expectedStep))
    }

    @Test
    fun `Given rename step and inputText, When change rename text, Then changed rename to inputText`() {
        val initStep = genRenameStep(renameText = "")
        val inputText = genBothify()
        val delegate = genTagEditDelegate(initStep = initStep)

        val expectedStep = initStep.copy(renameText = inputText)
        delegate.changeRenameText(inputText)
        assertThat(delegate.step.value, equalTo(expectedStep))
    }

    @Test
    fun `Given not rename step, When try update tag name, Then return success and not changed step`() = runTest {
        val initStep = genTagEditStepWithoutTypeOf<TagEditStep.Rename>()
        val delegate = genTagEditDelegate(initStep = initStep)

        val result = delegate.tryUpdateTagName(loadedTagsSnapshot = genTags())
        assertThat(result.isSuccess, equalTo(true))
        assertThat(delegate.step.value, equalTo(initStep))
    }

    @Test
    fun `Given empty input rename step, When try update tag name, Then return success and changed step to empty`() = runTest {
        val initStep = genRenameStep(renameText = "")
        val delegate = genTagEditDelegate(initStep = initStep)

        val result = delegate.tryUpdateTagName(loadedTagsSnapshot = genTags())
        assertThat(result.isSuccess, equalTo(true))
        assertThat(delegate.step.value, equalTo(TagEditStep.Empty))
    }

    @Test
    fun `Given rename step, tags and result of tryUpdateTagNameUseCase is Success or NotChanged, When try update tag name, Then return success and changed step to empty`() = runTest {
        suspend fun testTryUpdateTagName(tryUpdateTagNameResult: TryUpdateTagNameResult) {
            val initStep = genRenameStep()
            val tags = genTags()
            val delegate = genTagEditDelegate(
                initStep = initStep,
                tryUpdateTagNameUseCase = mock {
                    whenever(
                        mock.invoke(
                            tagId = initStep.tag.id,
                            newName = initStep.renameText.toNonBlankString(),
                            tagGroup = TagGroupSource.Snapshot(tags)
                        )
                    ).doReturn(tryUpdateTagNameResult)
                }
            )
            val result = delegate.tryUpdateTagName(loadedTagsSnapshot = tags)
            assertThat(result.isSuccess, equalTo(true))
            assertThat(delegate.step.value, equalTo(TagEditStep.Empty))
        }

        testTryUpdateTagName(TryUpdateTagNameResult.Success(genTag()))
        testTryUpdateTagName(TryUpdateTagNameResult.NotChanged)
    }

    @Test
    fun `Given rename step, tags and result of tryUpdateTagNameUseCase is DuplicateNameError, When try update tag name, Then return success and changed step to empty, merge`() = runTest {
        val targetTag = genTag()
        val duplicateTag = genTag()
        val initStep = genRenameStep(tag = targetTag)
        val tags = genTags()
        val delegate = genTagEditDelegate(
            initStep = initStep,
            tryUpdateTagNameUseCase = mock {
                whenever(
                    mock.invoke(
                        tagId = initStep.tag.id,
                        newName = initStep.renameText.toNonBlankString(),
                        tagGroup = TagGroupSource.Snapshot(tags)
                    )
                ).doSuspendableAnswer {
                    delay(1_000) // for prevent conflate.
                    TryUpdateTagNameResult.DuplicateNameError(duplicateTag)
                }
            }
        )
        val expectedSteps = listOf(
            initStep,
            TagEditStep.Empty,
            TagEditStep.Merge(from = targetTag, to = duplicateTag)
        )

        val actualSteps = mutableListOf<TagEditStep>()
        unconfinedBackgroundScope.launch { delegate.step.toList(actualSteps) }
        val result = delegate.tryUpdateTagName(tags)
        assertThat(result.isSuccess, equalTo(true))
        assertThat(actualSteps, equalTo(expectedSteps))
    }

    @Test
    fun `Given rename step, tags and result of tryUpdateTagNameUseCase is UnknownError, When try update tag name, Then return fail and changed step to empty`() = runTest {
        val initStep = genRenameStep()
        val tags = genTags()
        val delegate = genTagEditDelegate(
            initStep = initStep,
            tryUpdateTagNameUseCase = mock {
                whenever(
                    mock.invoke(
                        tagId = initStep.tag.id,
                        newName = initStep.renameText.toNonBlankString(),
                        tagGroup = TagGroupSource.Snapshot(tags)
                    )
                ).doReturn(TryUpdateTagNameResult.UnknownError)
            }
        )
        val result = delegate.tryUpdateTagName(tags)
        assertThat(result.isFailure, equalTo(true))
        assertThat(delegate.step.value, equalTo(TagEditStep.Empty))
    }

    @Test
    fun `Given not merge step, When merge tag, Then return success and changed step to empty`() = runTest {
        val initStep = genTagEditStepWithoutTypeOf<TagEditStep.Merge>()
        val delegate = genTagEditDelegate(initStep = initStep)

        val result = delegate.mergeTag()
        assertThat(result.isSuccess, equalTo(true))
        assertThat(delegate.step.value, equalTo(initStep))
    }

    @Test
    fun `Given merge tag and success of save, When merge tag, Then return success and changed step to empty`() = runTest {
        val initStep = genMergeStep()
        val delegate = genTagEditDelegate(
            initStep = initStep,
            tagRepository = mock {
                whenever(mock.save(query = SaveTagQuery.Modify(id = initStep.from.id, name = initStep.to.name)))
                    .doReturn(Result.Success(genTag()))
            }
        )

        val result = delegate.mergeTag()
        assertThat(result.isSuccess, equalTo(true))
        assertThat(delegate.step.value, equalTo(TagEditStep.Empty))
    }

    @Test
    fun `Given merge tag and fail of save, When merge tag, Then return fail and changed step to empty`() = runTest {
        val initStep = genMergeStep()
        val delegate = genTagEditDelegate(
            initStep = initStep,
            tagRepository = mock {
                whenever(mock.save(query = SaveTagQuery.Modify(id = initStep.from.id, name = initStep.to.name)))
                    .doReturn(Result.Failure(IllegalStateException()))
            }
        )

        val result = delegate.mergeTag()
        assertThat(result.isFailure, equalTo(true))
        assertThat(delegate.step.value, equalTo(TagEditStep.Empty))
    }

    @Test
    fun `Given intro tag, When start delete, Then changed step to delete`() {
        val step = genIntroStep()
        val delegate = genTagEditDelegate(initStep = step)
        delegate.startDelete()

        val expectedStep = TagEditStep.Delete(
            tag = step.tag,
            usageCount = step.usageCount
        )
        assertThat(delegate.step.value, equalTo(expectedStep))
    }

    @Test
    fun `Given not delete step, When delete tag, Then return success and changed step to empty`() = runTest {
        val initStep = genTagEditStepWithoutTypeOf<TagEditStep.Delete>()
        val delegate = genTagEditDelegate(initStep = initStep)

        val result = delegate.deleteTag()
        assertThat(result.isSuccess, equalTo(true))
        assertThat(delegate.step.value, equalTo(initStep))
    }

    @Test
    fun `Given delete step and success of delete, When delete tag, Then return success and changed step to empty`() = runTest {
        val initStep = genDeleteStep()
        val delegate = genTagEditDelegate(
            initStep = initStep,
            tagRepository = mock {
                whenever(mock.delete(initStep.tag.id)) doReturn Result.Success(Unit)
            }
        )

        val result = delegate.deleteTag()
        assertThat(result.isSuccess, equalTo(true))
        assertThat(delegate.step.value, equalTo(TagEditStep.Empty))
    }

    @Test
    fun `Given delete step and fail of delete, When delete tag, Then return fail and changed step to empty`() = runTest {
        val initStep = genDeleteStep()
        val delegate = genTagEditDelegate(
            initStep = initStep,
            tagRepository = mock {
                whenever(mock.delete(initStep.tag.id)) doReturn Result.Failure(IllegalStateException())
            }
        )

        val result = delegate.deleteTag()
        assertThat(result.isFailure, equalTo(true))
        assertThat(delegate.step.value, equalTo(TagEditStep.Empty))
    }

    @Test
    fun `Given not empty step, When clear step, Then change step to empty`() {
        val initStep = genTagEditStep()
        val delegate = genTagEditDelegate(initStep = initStep)
        delegate.clearStep()

        assertThat(delegate.step.value, equalTo(TagEditStep.Empty))
    }
}

private fun genTagEditDelegate(
    initStep: TagEditStep,
    tagRepository: TagRepository = mock(),
    tryUpdateTagNameUseCase: TryUpdateTagNameUseCase = mock(),
): TagEditDelegate = TagEditDelegate(initStep, tagRepository, tryUpdateTagNameUseCase)

private val sampleSteps: List<TagEditStep> get() = listOf(
    genIntroStep(),
    genRenameStep(),
    genMergeStep(),
    genDeleteStep()
)

private fun genTagEditStep(): TagEditStep =
    sampleSteps.shuffled().first()

private inline fun <reified T : TagEditStep> genTagEditStepWithoutTypeOf(): TagEditStep =
    sampleSteps.shuffleAndGetFirst { interaction -> interaction !is T }

private fun genIntroStep() = TagEditStep.Intro(tag = genTag(), usageCount = genNonNegativeLong())

private fun genRenameStep(
    tag: Tag = genTag(),
    usageCount: NonNegativeLong = genNonNegativeLong(),
    renameText: String = genBothify(),
    shouldUserInputReady: Boolean = genBoolean(),
) = TagEditStep.Rename(tag, usageCount, renameText, shouldUserInputReady)

private fun genMergeStep(
    from: Tag = genTag(),
    to: Tag = genTag(),
) = TagEditStep.Merge(from, to)

private fun genDeleteStep(
    tag: Tag = genTag(),
    usageCount: NonNegativeLong = genNonNegativeLong(),
) = TagEditStep.Delete(tag, usageCount)