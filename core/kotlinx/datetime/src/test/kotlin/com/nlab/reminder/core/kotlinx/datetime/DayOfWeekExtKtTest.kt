package com.nlab.reminder.core.kotlinx.datetime

import kotlinx.datetime.DayOfWeek
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class DayOfWeekExtKtTest {
    @Test
    fun assertWeekdays() {
        val weekdays = Weekdays()
        assertThat(
            weekdays, equalTo(
                setOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
                )
            )
        )
    }

    @Test
    fun assertWeekend() {
        val weekend = Weekend()
        assertThat(
            weekend, equalTo(
                setOf(
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY,
                )
            )
        )
    }

    @Test
    fun `weekdays is exactly weekdays`() {
        val weekdays = Weekdays()
        assertThat(weekdays.isExactlyWeekdays(), equalTo(true))
    }

    @Test
    fun `weekdays with one dropped is not exactly weekdays`() {
        val partialWeekdays = Weekdays()
            .drop(1)
            .toSet()
        assertThat(partialWeekdays.isExactlyWeekdays(), equalTo(false))
    }

    @Test
    fun `weekend is exactly weekend`() {
        val weekend = Weekend()
        assertThat(weekend.isExactlyWeekend(), equalTo(true))
    }

    @Test
    fun `weekend with one dropped is not exactly weekend`() {
        val partialWeekend = Weekend()
            .drop(1)
            .toSet()
        assertThat(partialWeekend.isExactlyWeekend(), equalTo(false))
    }
}