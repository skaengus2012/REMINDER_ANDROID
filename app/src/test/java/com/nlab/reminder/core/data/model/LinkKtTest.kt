package com.nlab.reminder.core.data.model

import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
class LinkKtTest {
    @Test
    fun testIsEmpty() {
        val link = Link(buildString {
            repeat(genInt(min = 1, max = 10)) { append(" ") }
        })
        assert(link.isEmpty())
    }

    @Test
    fun testOrEmpty() {
        val nullInstance: Link? = null
        assertThat(nullInstance.orEmpty(), equalTo(Link.EMPTY))
    }
}