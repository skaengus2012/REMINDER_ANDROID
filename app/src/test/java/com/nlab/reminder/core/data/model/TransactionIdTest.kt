package com.nlab.reminder.core.data.model

import org.junit.Test

/**
 * @author Doohyun
 */
internal class TransactionIdTest {
    @Test(expected = IllegalArgumentException::class)
    fun `TransactionId cannot be an empty value`() {
        TransactionId(value = "")
        TransactionId(value = "   ")
    }
}