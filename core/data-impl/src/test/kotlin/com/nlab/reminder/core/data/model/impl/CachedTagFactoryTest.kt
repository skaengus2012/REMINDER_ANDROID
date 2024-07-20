package com.nlab.reminder.core.data.model.impl

import com.nlab.reminder.core.data.model.TestCacheFactory
import com.nlab.reminder.core.data.model.genTagAndEntity
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
        val (expectedTag, expectedEntity) = genTagAndEntity()
        val factory = CachedTagFactory(
            internalFactory = mock(),
            cacheFactory = TestCacheFactory { key ->
                if (key == expectedEntity) expectedTag
                else error("Unknown key")
            },
            maxSize = genIntGreaterThanZero(),
        )
        val actualTag = factory.create(expectedEntity)
        assertThat(actualTag, equalTo(expectedTag))
    }
}