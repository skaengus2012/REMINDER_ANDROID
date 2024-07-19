package com.nlab.reminder.core.data.model

import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Doohyun
 */
internal class TagTest {
    @Test
    fun `Given not blank name, When make tag, Then success`() = runTest {
        val name = genBothify()
        Tag(id = TagId.Empty, name)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given blank name, When make tag, Then precondition failed`() = runTest {
        val emptyName = buildString {
            repeat(genInt(min = 0, max = 5)) {
                append(' ')
            }
        }
        Tag(id = TagId.Empty, emptyName)
    }
}