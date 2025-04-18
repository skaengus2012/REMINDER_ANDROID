package com.nlab.reminder.core.data.model

import com.nlab.testkit.faker.genBoolean
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class TriggerTimeTransformsKtTest {
    @Test
    fun `Given utcTime, isDateOnly is null, When create to triggerTime, Then result is null`() {
        val utcTime: Instant? = null
        val isDateOnly: Boolean? = null
        val actualTriggerTime = createTriggerTimeOrNull(utcTime, isDateOnly)
        assertThat(actualTriggerTime, nullValue())
    }

    @Test
    fun `Given utcTime, isDateOnly is nonnull, When create to triggerTime, Then return matched value`() {
        val utcTime = Clock.System.now()
        val isDateOnly = genBoolean()
        val actualTriggerTime = checkNotNull(createTriggerTimeOrNull(utcTime, isDateOnly))

        assertThat(actualTriggerTime.utcTime, equalTo(utcTime))
        assertThat(actualTriggerTime.isDateOnly, equalTo(isDateOnly))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given utcTime is null, isDateOnly is nonnull, When create to triggerTime, Then throw Exception`() {
        val utcTime: Instant? = null
        val isDateOnly = genBoolean()

        createTriggerTimeOrNull(utcTime, isDateOnly)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given utcTime is nonnull, isDateOnly is null, When create to triggerTime, Then throw Exception`() {
        val utcTime = Clock.System.now()
        val isDateOnly: Boolean? = null

        createTriggerTimeOrNull(utcTime, isDateOnly)
    }

    @Test
    fun `Given triggerTime, When convert to dto, Then return matched value`() {
        val triggerTime = genTriggerTime()
        val dto = triggerTime.toDTO()

        assertThat(dto.utcTime, equalTo(triggerTime.utcTime))
        assertThat(dto.isDateOnly, equalTo(triggerTime.isDateOnly))
    }
}