package com.nlab.statekit.internal

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class CollectionsKtTest {
    @Test
    fun `Given ordered number list, When collect number using forEachReversed, Then return reversed list`() {
        val orderedList = listOf(1, 2, 3, 4, 5)
        val expected = orderedList.reversed()

        val actual = buildList {
            orderedList.forEachReversed { num -> add(num) }
        }
        assertThat(actual, equalTo(expected))
    }
}