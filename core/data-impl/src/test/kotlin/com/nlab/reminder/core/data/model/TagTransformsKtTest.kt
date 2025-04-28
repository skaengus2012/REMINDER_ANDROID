package com.nlab.reminder.core.data.model

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class TagTransformsKtTest {
    @Test
    fun `Given tagEntity, When convert to Tag, Then return matching Tag`() {
        val (expectedTag, entity) = genTagAndEntity()
        val actualTag = Tag(entity)
        assertThat(actualTag, equalTo(expectedTag))
    }
}