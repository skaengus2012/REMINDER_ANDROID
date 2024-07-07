package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.model.genTagId
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.data.repository.TagGetQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.getOrThrow
import com.nlab.reminder.core.kotlin.isSuccess
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
internal class CachedTagRepositoryTest {
    @Test
    fun `When save tag, Then internal repository operated and cache updated`() = runTest {
        val newTag = genTag()
        val internalRepository = mock<TagRepository> {
            whenever(mock.save(newTag)) doReturn Result.Success(newTag)
        }
        val tagRepository = genTagRepository(
            internalRepository = internalRepository,
            coroutineScope = CoroutineScope(Dispatchers.Default)
        )
        val actualTagResult = tagRepository.save(newTag)

        assert(actualTagResult.getOrThrow() == newTag)
    }

    @Test
    fun `When delete tag, internal repository operated and cache updated`() = runTest {
        val targetId = genTagId()
        val internalRepository = mock<TagRepository> {
            whenever(mock.delete(targetId)) doReturn Result.Success(Unit)
        }
        val tagRepository = genTagRepository(
            internalRepository = internalRepository,
            coroutineScope = CoroutineScope(Dispatchers.Default)
        )
        val actualTagResult = tagRepository.delete(targetId)

        assert(actualTagResult.isSuccess)
    }

    @Test
    fun `When get tags by all query, Then cache saved from internal repository`() = runTest {
        val expectedTags = genTags()
        val internalRepository = mock<TagRepository> {
            whenever(mock.getTags(TagGetQuery.All)) doReturn Result.Success(expectedTags)
        }
        val tagRepository = genTagRepository(internalRepository = internalRepository)
        val actualTags = tagRepository.getTags(TagGetQuery.All).getOrThrow()
        assert(actualTags.toSet() == expectedTags.toSet())
    }

    @Test
    fun `Given saved all cache, When get tags, Then repository never invoked`() = runTest {
        val internalRepository = mock<TagRepository> {
            whenever(mock.getTags(TagGetQuery.All)) doReturn Result.Success(genTags())
        }
        val tagRepository =
            genTagRepository(internalRepository = internalRepository).apply { getTags(TagGetQuery.All) }

        tagRepository.getTags(TagGetQuery.All)
        tagRepository.getTags(TagGetQuery.ByIds(listOf(genTagId())))

        verify(
            internalRepository,
            once() // Cache has been added
        ).getTags(TagGetQuery.All)
    }

    @Test
    fun `Given saved partial cache, When get tags that hit all caches, Then repository never invoked`() = runTest {
        val tags = genTags()
        val query = TagGetQuery.ByIds(tags.map { it.id })
        val internalRepository = mock<TagRepository> {
            whenever(mock.getTags(query)) doReturn Result.Success(tags)
        }
        val tagRepository = genTagRepository(internalRepository = internalRepository).apply { getTags(query) }

        tagRepository.getTags(query)

        verify(
            internalRepository,
            once() // Cache has been added
        ).getTags(query)
    }

    @Test
    fun `Get tags stream from internal repository`() = runTest {
        val expectedTags = genTags()
        val internalRepository = mock<TagRepository> {
            whenever(mock.getTags(TagGetQuery.All)) doReturn Result.Success(expectedTags)
        }
        val tagRepository = genTagRepository(internalRepository = internalRepository)

        val actualTagsJob = CompletableDeferred<List<Tag>>()
        val collectJob = launch {
            tagRepository.getTagsAsStream(TagGetQuery.All)
                .collect { actualTags -> actualTagsJob.complete(actualTags) }
        }

        assert(actualTagsJob.await().toSet() == expectedTags.toSet())
        collectJob.cancelAndJoin()
    }
}

private fun genTagRepository(
    internalRepository: TagRepository,
    coroutineScope: CoroutineScope = mock()
): CachedTagRepository = CachedTagRepository(
    internalRepository = internalRepository,
    coroutineScope = coroutineScope
)