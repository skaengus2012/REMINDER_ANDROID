package com.nlab.reminder.core.schedule.model

import com.nlab.testkit.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class ScheduleElementsKtTest {
    @Test
    fun `Given correct position, When findPosition, Then return scheduleId`() {
        val position = genInt(min = 2, max = 4)
        val scheduleElements = genScheduleElements(position + 5)

        assertThat(scheduleElements.findId(position), equalTo(scheduleElements[position].id))
    }

    @Test
    fun `Given wrong position, When findPosition, Then return null`() {
        val position = genInt(min = 2, max = 4)
        val scheduleElements = genScheduleElements(position - 2)

        assertThat(scheduleElements.findId(position), equalTo(null))
    }
}