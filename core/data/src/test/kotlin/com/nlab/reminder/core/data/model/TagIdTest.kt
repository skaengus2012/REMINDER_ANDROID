package com.nlab.reminder.core.data.model

import com.nlab.testkit.faker.genLongGreaterThanZero
import org.junit.Test

/**
 * @author Doohyun
 */
internal class TagIdTest {
    @Test(expected = IllegalArgumentException::class)
    fun `Given zero, When created, Then the precondition failed`() {
        TagId(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given value less than 0, When created, Then the precondition failed`() {
        TagId(-genLongGreaterThanZero())
    }

    @Test
    fun `The created Empty objects are always equivalent`() {
        val expected = TagId.Empty
        val actual = TagId.Empty
        assert(actual == expected)
    }

    @Test
    fun `Given value greater than 0, When created, Than success`() {
        val given = genLongGreaterThanZero()
        val id = TagId(given)
        assert(id.value == given)
    }
}