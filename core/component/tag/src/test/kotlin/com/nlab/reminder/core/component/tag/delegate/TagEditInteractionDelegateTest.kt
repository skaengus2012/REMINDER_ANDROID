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

import com.nlab.reminder.core.component.tag.model.TagEditInteraction
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
import com.nlab.reminder.core.kotlin.faker.genNonBlankString
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.testkit.faker.genBlank
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.shuffleAndGetFirst
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
class TagEditInteractionDelegateTest {
    @Test
    fun `Given tag, When start editing, Then return intro`() = runTest {
        val inputTag = genTag()
        val expectedUsageCount = genNonNegativeLong()
        val expectedIntro = TagEditInteraction.Intro(inputTag, expectedUsageCount)
        val delegate = genTagEditInteractionDelegate(
            tagRepository = mock {
                whenever(mock.getUsageCount(inputTag.id)) doReturn Result.Success(expectedUsageCount)
            }
        )
        val result = delegate.startEditing(tag = inputTag)
        assertThat(result, equalTo(Result.Success(expectedIntro)))
    }

    @Test
    fun `Given intro interaction, When start rename, Then return rename interaction`() {
        val inputInteraction = genIntroInteraction()
        val expectedRenameInteraction = TagEditInteraction.Rename(
            tag = inputInteraction.tag,
            usageCount = inputInteraction.usageCount,
            renameText = inputInteraction.tag.name.value,
            shouldUserInputReady = true,
        )
        val delegate = genTagEditInteractionDelegate()
        val actualInteraction = delegate.startRename(inputInteraction)
        assertThat(actualInteraction, equalTo(expectedRenameInteraction))
    }

    @Test
    fun `Given any interaction without intro, When start rename, Then return input interaction`() {
        val inputInteraction = genTagEditInteractionWithoutTypeOf<TagEditInteraction.Intro>()
        val delegate = genTagEditInteractionDelegate()
        val actualInteraction = delegate.startRename(inputInteraction)
        assertThat(actualInteraction, sameInstance(inputInteraction))
    }

    @Test
    fun `Given rename interaction, When ready to rename input, Then return interaction that shouldUserInputReady is false`() {
        val inputInteraction = genRenameInteraction(shouldUserInputReady = true)
        val expectedInteraction = inputInteraction.copy(shouldUserInputReady = false)
        val delegate = genTagEditInteractionDelegate()
        val actualInteraction =delegate.readyRenameInput(inputInteraction)
        assertThat(actualInteraction, equalTo(expectedInteraction))
    }

    @Test
    fun `Given any interaction without rename, When ready to rename input, Then return input interaction`() {
        val inputInteraction = genTagEditInteractionWithoutTypeOf<TagEditInteraction.Rename>()
        val delegate = genTagEditInteractionDelegate()
        val actualInteraction = delegate.readyRenameInput(inputInteraction)
        assertThat(actualInteraction, sameInstance(inputInteraction))
    }

    @Test
    fun `Given inputText and rename interaction, When change rename text, Then return interaction that renameText changed`() {
        val inputText = genBothify()
        val inputInteraction = genRenameInteraction(renameText = "")
        val expectedInteraction = inputInteraction.copy(renameText = inputText)
        val delegate = genTagEditInteractionDelegate()
        val actualInteraction =delegate.changeRenameText(inputInteraction, inputText)
        assertThat(actualInteraction, equalTo(expectedInteraction))
    }

    @Test
    fun `Given inputText and any interaction without rename, When change rename text, Then return input interaction`() {
        val inputText = genBothify()
        val inputInteraction = genTagEditInteractionWithoutTypeOf<TagEditInteraction.Rename>()
        val delegate = genTagEditInteractionDelegate()
        val actualInteraction = delegate.changeRenameText(inputInteraction, inputText)
        assertThat(actualInteraction, sameInstance(inputInteraction))
    }

    @Test
    fun `Given any interaction without rename, When try update tag name, Then return a flow that emits a identity LazyTagEditInteractionResult`() = runTest {
        val initInteraction = genTagEditInteractionWithoutTypeOf<TagEditInteraction.Rename>()
        val delegate = genTagEditInteractionDelegate()
        val actualResults = delegate.tryUpdateTagName(initInteraction, genTags()).toList()
        assertThat(actualResults.size, equalTo(1))

        val nextInteractionFunc = (actualResults.first() as LazyTagEditInteractionResult.NextInteraction)
        assertThat(nextInteractionFunc(initInteraction), sameInstance(initInteraction))
    }

    @Test
    fun `Given blank input rename interaction, When try update tag name, Then return a flow that emits a terminable LazyTagEditInteractionResult`() = runTest {
        val initInteraction = genRenameInteraction(renameText = genBlank())
        val delegate = genTagEditInteractionDelegate()
        val actualResults = delegate.tryUpdateTagName(initInteraction, genTags()).toList()
        assertThat(actualResults.size, equalTo(1))

        val nextInteractionFunc = (actualResults.first() as LazyTagEditInteractionResult.NextInteraction)
        assertThat(nextInteractionFunc(initInteraction), nullValue())

        val newInteraction = initInteraction.copy(renameText = genBothify())
        assertThat(nextInteractionFunc(newInteraction), sameInstance(newInteraction))
    }

    @Test
    fun `Given rename interaction, When try update tag name and tryUpdateTagName return not unknownError, Then return results exclude UnknownError`() = runTest {
        suspend fun testTryUpdateTagNameResultSuccess(
            expectedTryUpdateTagNameResult: TryUpdateTagNameResult,
        ) {
            val tag = genTag()
            val renameText = genNonBlankString()
            val tags = genTags()
            val delegate = genTagEditInteractionDelegate(
                tryUpdateTagNameUseCase = mock {
                    whenever(mock.invoke(tag.id, renameText, TagGroupSource.Snapshot(tags))) doReturn expectedTryUpdateTagNameResult
                }
            )
            val actualResults = delegate
                .tryUpdateTagName(
                    genRenameInteraction(tag = tag, renameText = renameText.value),
                    genTags()
                )
                .toList()
            assert(actualResults.all { it !is LazyTagEditInteractionResult.UnknownError })
        }

        testTryUpdateTagNameResultSuccess(expectedTryUpdateTagNameResult = TryUpdateTagNameResult.Success(genTag()))
        testTryUpdateTagNameResultSuccess(expectedTryUpdateTagNameResult = TryUpdateTagNameResult.NotChanged)
        testTryUpdateTagNameResultSuccess(expectedTryUpdateTagNameResult = TryUpdateTagNameResult.DuplicateNameError(genTag()))
    }

    @Test
    fun `Given rename interaction, When try update tag name and tryUpdateTagName return unknownError, Then return results include UnknownError`() = runTest {
        val tag = genTag()
        val renameText = genNonBlankString()
        val tags = genTags()
        val delegate = genTagEditInteractionDelegate(
            tryUpdateTagNameUseCase = mock {
                whenever(mock.invoke(tag.id, renameText, TagGroupSource.Snapshot(tags))) doReturn TryUpdateTagNameResult.UnknownError
            }
        )
        val actualResults = delegate
            .tryUpdateTagName(genRenameInteraction(tag = tag, renameText = renameText.value), tags)
            .toList()
        assert(actualResults.size == 2)
        assertThat(
            actualResults.last(),
            equalTo(LazyTagEditInteractionResult.UnknownError)
        )
    }

    @Test
    fun `Given rename interaction, When try update tag name and tryUpdateTagName return duplicateNameError, Then a flow that emits a merge able LazyTagEditInteractionResult`() = runTest {
        val tag = genTag()
        val renameText = genNonBlankString()
        val duplicateTag = tag.copy(name = (tag.name.value + "_duplicate").toNonBlankString())
        val initInteraction = genRenameInteraction(tag = tag, renameText = renameText.value)
        val delegate = genTagEditInteractionDelegate(
            tryUpdateTagNameUseCase = mock {
                whenever(mock.invoke(tag.id, renameText, TagGroupSource.Snapshot(listOf(duplicateTag))))
                    .doReturn(TryUpdateTagNameResult.DuplicateNameError(duplicateTag))
            }
        )

        val actualResults = delegate
            .tryUpdateTagName(initInteraction, listOf(duplicateTag))
            .toList()
        assert(actualResults.size == 2)

        val nextInteractionFunc = (actualResults.last() as LazyTagEditInteractionResult.NextInteraction)
        assertThat(
            nextInteractionFunc(null),
            equalTo(TagEditInteraction.Merge(from = tag, to = duplicateTag))
        )

        val newInteraction = genTagEditInteraction()
        assertThat(
            nextInteractionFunc(newInteraction),
            sameInstance(newInteraction)
        )
    }

    @Test
    fun `Given any interaction without merge, When merge tag, Then return a flow that emits a identity LazyTagEditInteractionResult`() = runTest {
        val interaction = genTagEditInteractionWithoutTypeOf<TagEditInteraction.Merge>()
        val delegate = genTagEditInteractionDelegate()
        val actualResults = delegate.mergeTag(interaction).toList()
        assertThat(actualResults.size, equalTo(1))

        val nextInteraction = (actualResults.first() as LazyTagEditInteractionResult.NextInteraction)(interaction)
        assertThat(nextInteraction, sameInstance(interaction))
    }

    @Test
    fun `Given merge interaction, When merge tag, Then tagRepository called save`() = runTest {
        val from = genTag()
        val to = genTag(name = (from.name.value + "duplicated").toNonBlankString())
        val interaction = TagEditInteraction.Merge(
            from = from,
            to = to,
        )
        val tagRepository: TagRepository = mock {
            whenever(mock.save(any())) doReturn Result.Success(genTag(from.id, to.name))
        }
        val delegate = genTagEditInteractionDelegate(tagRepository = tagRepository)
        delegate.mergeTag(interaction).toList()
        verify(tagRepository, once()).save(SaveTagQuery.Modify(id = from.id, name = to.name))
    }

    @Test
    fun `Given merge interaction, When merge tag and repository occurred error, Then return results include UnknownError`() = runTest {
        val from = genTag()
        val to = genTag(name = (from.name.value + "duplicated").toNonBlankString())
        val interaction = TagEditInteraction.Merge(
            from = from,
            to = to,
        )
        val tagRepository: TagRepository = mock {
            whenever(mock.save(any())) doReturn Result.Failure(RuntimeException())
        }
        val delegate = genTagEditInteractionDelegate(tagRepository = tagRepository)
        val actualResults = delegate.mergeTag(interaction).toList()
        assertThat(actualResults.size, equalTo(2))
        assertThat(
            actualResults.last(),
            equalTo(LazyTagEditInteractionResult.UnknownError)
        )
    }

    @Test
    fun `Given intro interaction, When delete tag, Then return delete interaction`() {
        val inputInteraction = genIntroInteraction()
        val expectedRenameInteraction = TagEditInteraction.Delete(
            tag = inputInteraction.tag,
            usageCount = inputInteraction.usageCount,
        )
        val delegate = genTagEditInteractionDelegate()
        val actualInteraction = delegate.startDelete(inputInteraction)
        assertThat(actualInteraction, equalTo(expectedRenameInteraction))
    }

    @Test
    fun `Given any interaction without intro, When delete tag, Then return input interaction`() {
        val inputInteraction = genTagEditInteractionWithoutTypeOf<TagEditInteraction.Intro>()
        val delegate = genTagEditInteractionDelegate()
        val actualInteraction = delegate.startDelete(inputInteraction)
        assertThat(actualInteraction, sameInstance(inputInteraction))
    }

    @Test
    fun `Given any interaction without delete, When delete tag, Then return a flow that emits a identity LazyTagEditInteractionResult`() = runTest {
        val interaction = genTagEditInteractionWithoutTypeOf<TagEditInteraction.Delete>()
        val delegate = genTagEditInteractionDelegate()
        val actualResults = delegate.deleteTag(interaction).toList()
        assertThat(actualResults.size, equalTo(1))

        val nextInteraction = (actualResults.first() as LazyTagEditInteractionResult.NextInteraction)(interaction)
        assertThat(nextInteraction, sameInstance(interaction))
    }

    @Test
    fun `Given delete interaction, When delete tag, Then tagRepository called delete`() = runTest {
        val interaction = TagEditInteraction.Delete(
            genTag(),
            genNonNegativeLong()
        )
        val tagRepository: TagRepository = mock {
            whenever(mock.delete(interaction.tag.id)) doReturn Result.Success(Unit)
        }
        val delegate = genTagEditInteractionDelegate(tagRepository = tagRepository)
        delegate.deleteTag(interaction).toList()
        verify(tagRepository, once()).delete(interaction.tag.id)
    }

    @Test
    fun `Given delete interaction, When delete tag and repository occurred error, Then return results include UnknownError`() = runTest {
        val interaction = TagEditInteraction.Delete(
            genTag(),
            genNonNegativeLong()
        )
        val tagRepository: TagRepository = mock {
            whenever(mock.delete(interaction.tag.id)) doReturn Result.Failure(RuntimeException())
        }
        val delegate = genTagEditInteractionDelegate(tagRepository = tagRepository)
        val actualResults = delegate.deleteTag(interaction).toList()
        assertThat(actualResults.size, equalTo(2))
        assertThat(
            actualResults.last(),
            equalTo(LazyTagEditInteractionResult.UnknownError)
        )
    }
}

private fun genTagEditInteractionDelegate(
    tagRepository: TagRepository = mock(),
    tryUpdateTagNameUseCase: TryUpdateTagNameUseCase = mock(),
): TagEditInteractionDelegate = TagEditInteractionDelegate(tagRepository, tryUpdateTagNameUseCase)

private val sampleInteraction: List<TagEditInteraction> get() = listOf(
    genIntroInteraction(),
    genRenameInteraction(),
    TagEditInteraction.Merge(
        from = genTag(),
        to = genTag(),
    ),
    TagEditInteraction.Delete(
        tag = genTag(),
        usageCount = genNonNegativeLong(),
    )
)

private fun genTagEditInteraction(): TagEditInteraction =
    sampleInteraction.shuffled().first()

private inline fun <reified T : TagEditInteraction> genTagEditInteractionWithoutTypeOf(): TagEditInteraction =
    sampleInteraction.shuffleAndGetFirst { interaction -> interaction !is T }

private fun genIntroInteraction() = TagEditInteraction.Intro(tag = genTag(), usageCount = genNonNegativeLong())

private fun genRenameInteraction(
    tag: Tag = genTag(),
    usageCount: NonNegativeLong = genNonNegativeLong(),
    renameText: String = genBothify(),
    shouldUserInputReady: Boolean = genBoolean(),
) = TagEditInteraction.Rename(tag, usageCount, renameText, shouldUserInputReady)