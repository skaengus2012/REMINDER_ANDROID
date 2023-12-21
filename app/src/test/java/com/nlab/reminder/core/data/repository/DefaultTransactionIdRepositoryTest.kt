package com.nlab.reminder.core.data.repository

import com.nlab.testkit.genBothify
import com.nlab.testkit.genLong
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class DefaultTransactionIdRepositoryTest {
    @Test
    fun testGenerate() = runTest {
        val randomPrefix = genBothify()
        val randomTimeStamp = genLong()
        val transactionIdRepository = DefaultTransactionIdRepository(
            randomPrefix = { randomPrefix },
            timestamp = { randomTimeStamp }
        )

        assertThat(transactionIdRepository.generate().value, equalTo("${randomPrefix}_${randomTimeStamp}"))
    }
}