package com.nlab.reminder.core.domain

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
internal class CalculateItemSwapResultUseCaseTest {
    @Test
    fun `Given same positions, Then returns emptyList`() {
        testWhenIllegalPositionsGetEmptyList(
            items = (0..10).toList(),
            fromPosition = 5,
            toPosition = 5
        )
    }

    @Test
    fun `Given illegal input positions, Then returns emptyList`() {
        testWhenIllegalPositionsGetEmptyList(
            items = (0..10).toList(),
            fromPosition = -1,
            toPosition = 5
        )

        testWhenIllegalPositionsGetEmptyList(
            items = (0..10).toList(),
            fromPosition = 11,
            toPosition = 5
        )

        testWhenIllegalPositionsGetEmptyList(
            items = (0..10).toList(),
            fromPosition = 5,
            toPosition = 20
        )

        testWhenIllegalPositionsGetEmptyList(
            items = (0..10).toList(),
            fromPosition = 5,
            toPosition = -1
        )
    }

    @Test
    fun `When item is moved down, Then returns movement result`() {
        val items = (0..10).toList()
        val result = CalculateItemSwapResultUseCase().invoke(items, fromPosition = 1, toPosition = 4)
        assertThat(result, equalTo(listOf(2, 3, 4, 1)))
    }

    @Test
    fun `When item is moved up, Then returns movement result`() {
        val items = (0..10).toList()
        val result = CalculateItemSwapResultUseCase().invoke(items, fromPosition = 6, toPosition = 1)
        assertThat(result, equalTo(listOf(6, 1, 2, 3, 4, 5)))
    }
}

private fun testWhenIllegalPositionsGetEmptyList(
    items: List<Int>,
    fromPosition: Int,
    toPosition: Int
) {
    val result = CalculateItemSwapResultUseCase().invoke(items, fromPosition, toPosition)
    assertThat(result, equalTo(emptyList()))
}