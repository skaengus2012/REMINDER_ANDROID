package com.nlab.reminder.core.data.model

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class DaysOfMonthTransformsKtTest {
    @Test
    fun `DaysOfMonth func should convert number to DaysOfMonth`() {
        assertThat(DaysOfMonth(1), sameInstance(DaysOfMonth.DAY_1))
        assertThat(DaysOfMonth(2), sameInstance(DaysOfMonth.DAY_2))
        assertThat(DaysOfMonth(3), sameInstance(DaysOfMonth.DAY_3))
        assertThat(DaysOfMonth(4), sameInstance(DaysOfMonth.DAY_4))
        assertThat(DaysOfMonth(5), sameInstance(DaysOfMonth.DAY_5))
        assertThat(DaysOfMonth(6), sameInstance(DaysOfMonth.DAY_6))
        assertThat(DaysOfMonth(7), sameInstance(DaysOfMonth.DAY_7))
        assertThat(DaysOfMonth(8), sameInstance(DaysOfMonth.DAY_8))
        assertThat(DaysOfMonth(9), sameInstance(DaysOfMonth.DAY_9))
        assertThat(DaysOfMonth(10), sameInstance(DaysOfMonth.DAY_10))
        assertThat(DaysOfMonth(11), sameInstance(DaysOfMonth.DAY_11))
        assertThat(DaysOfMonth(12), sameInstance(DaysOfMonth.DAY_12))
        assertThat(DaysOfMonth(13), sameInstance(DaysOfMonth.DAY_13))
        assertThat(DaysOfMonth(14), sameInstance(DaysOfMonth.DAY_14))
        assertThat(DaysOfMonth(15), sameInstance(DaysOfMonth.DAY_15))
        assertThat(DaysOfMonth(16), sameInstance(DaysOfMonth.DAY_16))
        assertThat(DaysOfMonth(17), sameInstance(DaysOfMonth.DAY_17))
        assertThat(DaysOfMonth(18), sameInstance(DaysOfMonth.DAY_18))
        assertThat(DaysOfMonth(19), sameInstance(DaysOfMonth.DAY_19))
        assertThat(DaysOfMonth(20), sameInstance(DaysOfMonth.DAY_20))
        assertThat(DaysOfMonth(21), sameInstance(DaysOfMonth.DAY_21))
        assertThat(DaysOfMonth(22), sameInstance(DaysOfMonth.DAY_22))
        assertThat(DaysOfMonth(23), sameInstance(DaysOfMonth.DAY_23))
        assertThat(DaysOfMonth(24), sameInstance(DaysOfMonth.DAY_24))
        assertThat(DaysOfMonth(25), sameInstance(DaysOfMonth.DAY_25))
        assertThat(DaysOfMonth(26), sameInstance(DaysOfMonth.DAY_26))
        assertThat(DaysOfMonth(27), sameInstance(DaysOfMonth.DAY_27))
        assertThat(DaysOfMonth(28), sameInstance(DaysOfMonth.DAY_28))
        assertThat(DaysOfMonth(29), sameInstance(DaysOfMonth.DAY_29))
        assertThat(DaysOfMonth(30), sameInstance(DaysOfMonth.DAY_30))
        assertThat(DaysOfMonth(31), sameInstance(DaysOfMonth.DAY_31))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given number zero, When convert DaysOfMonth, Then throw required exception`() {
        DaysOfMonth(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given number 32, When convert DaysOfMonth, Then throw required exception`() {
        DaysOfMonth(32)
    }

    @Test
    fun `Should convert DaysOfMonth to raw value`() {
        assertThat(DaysOfMonth.DAY_1.rawValue, equalTo(1))
        assertThat(DaysOfMonth.DAY_2.rawValue, equalTo(2))
        assertThat(DaysOfMonth.DAY_3.rawValue, equalTo(3))
        assertThat(DaysOfMonth.DAY_4.rawValue, equalTo(4))
        assertThat(DaysOfMonth.DAY_5.rawValue, equalTo(5))
        assertThat(DaysOfMonth.DAY_6.rawValue, equalTo(6))
        assertThat(DaysOfMonth.DAY_7.rawValue, equalTo(7))
        assertThat(DaysOfMonth.DAY_8.rawValue, equalTo(8))
        assertThat(DaysOfMonth.DAY_9.rawValue, equalTo(9))
        assertThat(DaysOfMonth.DAY_10.rawValue, equalTo(10))
        assertThat(DaysOfMonth.DAY_11.rawValue, equalTo(11))
        assertThat(DaysOfMonth.DAY_12.rawValue, equalTo(12))
        assertThat(DaysOfMonth.DAY_13.rawValue, equalTo(13))
        assertThat(DaysOfMonth.DAY_14.rawValue, equalTo(14))
        assertThat(DaysOfMonth.DAY_15.rawValue, equalTo(15))
        assertThat(DaysOfMonth.DAY_16.rawValue, equalTo(16))
        assertThat(DaysOfMonth.DAY_17.rawValue, equalTo(17))
        assertThat(DaysOfMonth.DAY_18.rawValue, equalTo(18))
        assertThat(DaysOfMonth.DAY_19.rawValue, equalTo(19))
        assertThat(DaysOfMonth.DAY_20.rawValue, equalTo(20))
        assertThat(DaysOfMonth.DAY_21.rawValue, equalTo(21))
        assertThat(DaysOfMonth.DAY_22.rawValue, equalTo(22))
        assertThat(DaysOfMonth.DAY_23.rawValue, equalTo(23))
        assertThat(DaysOfMonth.DAY_24.rawValue, equalTo(24))
        assertThat(DaysOfMonth.DAY_25.rawValue, equalTo(25))
        assertThat(DaysOfMonth.DAY_26.rawValue, equalTo(26))
        assertThat(DaysOfMonth.DAY_27.rawValue, equalTo(27))
        assertThat(DaysOfMonth.DAY_28.rawValue, equalTo(28))
        assertThat(DaysOfMonth.DAY_29.rawValue, equalTo(29))
        assertThat(DaysOfMonth.DAY_30.rawValue, equalTo(30))
        assertThat(DaysOfMonth.DAY_31.rawValue, equalTo(31))
    }
}