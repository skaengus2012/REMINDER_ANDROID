package com.nlab.reminder.domain

import org.junit.Assert
import org.junit.Test


/**
 * @author Doohyun
 */
class MathUtilTest {
    @Test
    fun test() {
        Assert.assertEquals(3,  MathUtil().sum(1, 2))
    }
}