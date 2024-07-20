package com.nlab.reminder.core.data.model.impl

import com.nlab.reminder.core.data.model.genTagAndEntity
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class DefaultTagFactoryTest {
    @Test
    fun testCreate() {
        val (expectedTag, expectedEntity) = genTagAndEntity()
        val factory = DefaultTagFactory()
        val actualTag = factory.create(expectedEntity)
        assertThat(actualTag, equalTo(expectedTag))
    }
}