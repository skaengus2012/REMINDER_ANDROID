package com.nlab.reminder.core.schedule

import com.nlab.reminder.core.data.model.genSchedules
import com.nlab.testkit.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
internal class ScheduleItemsKtTest {
    @Test
    fun testToItems() {
        val schedules = genSchedules(genInt(min = 2, max = 10))
        assertThat(
            schedules.toItems(),
            equalTo(List(schedules.size) { ScheduleItem(schedules[it]) })
        )
    }
}