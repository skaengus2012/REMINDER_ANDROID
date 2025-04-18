package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.reminder.core.kotlin.faker.genPositiveInt
import com.nlab.reminder.core.local.database.model.REPEAT_MONTHLY
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.ScheduleWithDetailsEntity
import com.nlab.testkit.faker.genBlank
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genLong
import kotlinx.datetime.Clock
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class ScheduleTransformsKtTest {
    /**
    @Test
    fun `Given entity with expected Id, When convert to schedule, Then Schedule include expected value`() {
        val expectedId = genLong()
        val entity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(scheduleId = expectedId))
        }
        val actualSchedule = Schedule(entity)
        assertThat(actualSchedule.id.rawId, equalTo(expectedId))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given entity with negative visiblePriority, When convert to schedule, Then throw exception`() {
        val invalidVisiblePriority = genLong(min = -100, max = -1)
        val entity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(visiblePriority = invalidVisiblePriority))
        }
        Schedule(entity)
    }

    @Test
    fun `Given entity with expected visiblePriority, When convert to schedule, then schedule include expected value`() {
        val expectedVisiblePriority = genNonNegativeLong().value
        val entity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(visiblePriority = expectedVisiblePriority))
        }
        val actualSchedule = Schedule(entity)
        assertThat(actualSchedule.visiblePriority.value, equalTo(expectedVisiblePriority))
    }

    @Test
    fun `Given entity with expected complete, When convert to schedule, Then schedule include expected value`() {
        val expectedComplete = genBoolean()
        val entity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(isComplete = expectedComplete))
        }
        val actualSchedule = Schedule(entity)
        assertThat(actualSchedule.isComplete, equalTo(expectedComplete))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given title is blank, When convert to content, Then throw Error`() {
        val entity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(title = genBlank()))
        }
        ScheduleContent(entity)
    }

    @Test
    fun `Given description is null or blank, When convert to content, Then content note is null`() {
        fun assertScheduleNoteNull(scheduleContent: ScheduleContent) {
            assertThat(scheduleContent.note, nullValue())
        }

        val descriptionNullEntity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(description = null))
        }
        assertScheduleNoteNull(ScheduleContent(descriptionNullEntity))

        val descriptionBlankEntity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(description = genBlank()))
        }
        assertScheduleNoteNull(ScheduleContent(descriptionBlankEntity))
    }

    @Test
    fun `Given link is null or blank, When convert to content, Then content link is null`() {
        fun assertScheduleLinkNull(scheduleContent: ScheduleContent) {
            assertThat(scheduleContent.link, nullValue())
        }

        val linkNullEntity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(link = null))
        }
        assertScheduleLinkNull(ScheduleContent(linkNullEntity))

        val linkBlankEntity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(link = genBlank()))
        }
        assertScheduleLinkNull(ScheduleContent(linkBlankEntity))
    }

    @Test
    fun `Given triggerTimeElements is null, When convert to content, Then content triggerTime is null`() {
        val notExistedTriggerTimeEntity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(triggerTimeUtc = null, isTriggerTimeDateOnly = null))
        }
        val actualContent = ScheduleContent(notExistedTriggerTimeEntity)
        assertThat(actualContent.triggerTime, nullValue())
    }

    @Test
    fun `Given triggerTimeElements is existed, When convert to content, Then content triggerTime exist`() {
        val expectedTriggerTime = genTriggerTime()
        val existedTriggerTimeEntity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(
                schedule = schedule.copy(
                    triggerTimeUtc = expectedTriggerTime.utcTime,
                    isTriggerTimeDateOnly = expectedTriggerTime.isDateOnly
                )
            )
        }
        val actualContent = ScheduleContent(existedTriggerTimeEntity)
        assertThat(actualContent.triggerTime, equalTo(expectedTriggerTime))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given triggerTimeUtc null, isTriggerTimeDateOnly nonnull, When convert to content, Then throw Exception`() {
        val invalidEntity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(triggerTimeUtc = null, isTriggerTimeDateOnly = genBoolean()))
        }
        ScheduleContent(invalidEntity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given triggerTimeUtc nonnull, isTriggerTimeDateOnly null, When convert to content, Then throw Exception`() {
        val invalidEntity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(triggerTimeUtc = Clock.System.now(), isTriggerTimeDateOnly = null))
        }
        ScheduleContent(invalidEntity)
    }

    @Test
    fun `Given repeatType and repeatInterval is null, When convert to content, then content repeat is null`() {
        val notExistedRepeatEntity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(repeatType = null, repeatInterval = null))
        }
        val actualContent = ScheduleContent(notExistedRepeatEntity)
        assertThat(actualContent.repeat, nullValue())
    }

    @Test
    fun `Given repeatElements is existed, When convert to content, Then content repeat exist`() {
        val expectedRepeat = genRepeat()
        val existedTriggerTimeEntity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(
                schedule = schedule.copy(
                    repeatType = expectedRepeat.repeatType,
                    repeatInterval = expectedRepeat.interval.value,
                ),
                repeatDetails = expectedRepeat.toRepeatDetailDTOs().toSet { dto ->
                    RepeatDetailEntity(
                        scheduleId = schedule.scheduleId,
                        propertyCode = dto.propertyCode,
                        value = dto.value
                    )
                }
            )
        }
        val actualContent = ScheduleContent(existedTriggerTimeEntity)
        assertThat(actualContent.repeat, equalTo(expectedRepeat))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given repeatType null, repeatInterval nonnull, When convert to repeat, Then throw exception`() {
        val invalidEntity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(repeatType = null, repeatInterval = genPositiveInt().value))
        }
        ScheduleContent(invalidEntity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given repeatType nonnull, repeatInterval null, When convert to repeat, Then throw exception`() {
        val invalidEntity: ScheduleWithDetailsEntity = genScheduleWithDetailsEntity {
            copy(schedule = schedule.copy(repeatType = genRepeat().repeatType, repeatInterval = null))
        }
        ScheduleContent(invalidEntity)
    }

    @Test
    fun `Given content with nonnull state, When convert to contentDTO, Then contentDTO include matched value`() {
        val expectedUtcTime = Clock.System.now()
        val expectedIsDateOnly = genBoolean()
        val expectedRepeatType = REPEAT_MONTHLY
        val expectedInterval = genPositiveInt()
        val repeat = genRepeatMonthly(interval = expectedInterval)
        val content = genScheduleContent(
            triggerTime = TriggerTime(expectedUtcTime, expectedIsDateOnly),
            repeat = repeat
        )

        val actualContentDTO = content.toDTO()
        assertThat(content.title, equalTo(actualContentDTO.title))
        assertThat(content.note, equalTo(actualContentDTO.description))
        assertThat(content.link!!.rawLink, equalTo(actualContentDTO.link))
        assertThat(expectedUtcTime, equalTo(actualContentDTO.triggerTimeDTO!!.utcTime))
        assertThat(expectedIsDateOnly, equalTo(actualContentDTO.triggerTimeDTO!!.isDateOnly))
        assertThat(expectedRepeatType, equalTo(actualContentDTO.repeatDTO!!.type))
        assertThat(expectedInterval, equalTo(actualContentDTO.repeatDTO!!.interval))
        assertThat(repeat.toRepeatDetailDTOs(), equalTo(actualContentDTO.repeatDTO!!.details))
    }*/
}

private fun genScheduleWithDetailsEntity(
    block: ScheduleWithDetailsEntity.() -> ScheduleWithDetailsEntity
): ScheduleWithDetailsEntity {
    val scheduleAndEntity = genScheduleAndEntity()
    return scheduleAndEntity.second.let(block)
}
