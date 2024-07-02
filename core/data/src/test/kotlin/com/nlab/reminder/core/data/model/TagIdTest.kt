package com.nlab.reminder.core.data.model

import com.nlab.testkit.faker.genLongGreaterThanZero
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class TagIdTest {
    @Test(expected = IllegalArgumentException::class)
    fun `When a value less then 0 is given, Then contract error occurred`() {
        val given = -genLongGreaterThanZero()
        TagId(value = given)
    }

    @Test
    fun `The created Empty objects are always equivalent`() {
        val expected = TagId.Empty
        val actual = TagId.Empty
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `Given number greater than zero, When constructed, Than created TagId`() {
        val given = genLongGreaterThanZero()
        val id = TagId(given)
        assert(id.value == given)
    }
}