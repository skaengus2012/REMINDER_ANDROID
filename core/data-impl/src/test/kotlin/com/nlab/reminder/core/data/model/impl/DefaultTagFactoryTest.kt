package com.nlab.reminder.core.data.model.impl

import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.model.toEntity
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class DefaultTagFactoryTest {
    @Test
    fun testCreate() {
        val expectedTag = genTag()
        val factory = DefaultTagFactory()
        val actualTag = factory.create(expectedTag.toEntity())
        assertThat(actualTag, equalTo(expectedTag))
    }
}