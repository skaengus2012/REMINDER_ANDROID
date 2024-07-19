package com.nlab.reminder.core.data.model.impl

import com.nlab.reminder.core.data.model.TestCacheFactory
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.model.toEntity
import com.nlab.testkit.faker.genIntGreaterThanZero
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * @author Doohyun
 */
internal class CachedTagFactoryTest {
    @Test
    fun `When create, Then get from cache`() {
        val expectedTag = genTag()
        val tagEntity = expectedTag.toEntity()
        val factory = CachedTagFactory(
            internalFactory = mock(),
            cacheFactory = TestCacheFactory { key ->
                if (key == tagEntity) expectedTag
                else error("Unknown key")
            },
            maxSize = genIntGreaterThanZero(),
        )
        val actualTag = factory.create(tagEntity)
        assertThat(actualTag, equalTo(expectedTag))
    }
}