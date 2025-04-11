package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.reminder.core.kotlin.faker.genPositiveInt
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import com.nlab.reminder.core.local.database.model.ScheduleWithDetailsEntity
import com.nlab.testkit.faker.genBlank
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genLong
import kotlinx.datetime.Clock
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

/**
 * @author Doohyun
 */
class ScheduleTransformsKtTest {
    private lateinit var generatedEntity: ScheduleWithDetailsEntity

    @Before
    fun setup() {
        generatedEntity = genScheduleAndEntity().second
    }

    @Test
    fun `Given entity with expected Id, When convert to schedule, Then Schedule include expected value`() {
        val expectedId = genLong()
        val entity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(scheduleId = expectedId))
        }
        val actualSchedule = Schedule(entity)
        assertThat(actualSchedule.id.rawId, equalTo(expectedId))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given entity with negative visiblePriority, When convert to schedule, Then throw exception`() {
        val invalidVisiblePriority = genLong(min = -100, max = -1)
        val entity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(visiblePriority = invalidVisiblePriority))
        }
        Schedule(entity)
    }

    @Test
    fun `Given entity with expected visiblePriority, When convert to schedule, then schedule include expected value`() {
        val expectedVisiblePriority = genNonNegativeLong().value
        val entity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(visiblePriority = expectedVisiblePriority))
        }
        val actualSchedule = Schedule(entity)
        assertThat(actualSchedule.visiblePriority.value, equalTo(expectedVisiblePriority))
    }

    @Test
    fun `Given entity with expected complete, When convert to schedule, Then schedule include expected value`() {
        val expectedComplete = genBoolean()
        val entity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(isComplete = expectedComplete))
        }
        val actualSchedule = Schedule(entity)
        assertThat(actualSchedule.isComplete, equalTo(expectedComplete))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given title is blank, When convert to content, Then throw Error`() {
        val entity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(title = genBlank()))
        }
        ScheduleContent(entity)
    }

    @Test
    fun `Given description is null or blank, When convert to content, Then content note is null`() {
        fun assertScheduleNoteNull(scheduleContent: ScheduleContent) {
            assertThat(scheduleContent.note, nullValue())
        }

        val descriptionNullEntity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(description = null))
        }
        assertScheduleNoteNull(ScheduleContent(descriptionNullEntity))

        val descriptionBlankEntity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(description = genBlank()))
        }
        assertScheduleNoteNull(ScheduleContent(descriptionBlankEntity))
    }

    @Test
    fun `Given link is null or blank, When convert to content, Then content link is null`() {
        fun assertScheduleLinkNull(scheduleContent: ScheduleContent) {
            assertThat(scheduleContent.link, nullValue())
        }

        val linkNullEntity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(link = null))
        }
        assertScheduleLinkNull(ScheduleContent(linkNullEntity))

        val linkBlankEntity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(link = genBlank()))
        }
        assertScheduleLinkNull(ScheduleContent(linkBlankEntity))
    }

    @Test
    fun `Given triggerTimeElements is null, When convert to content, Then content triggerTime is null`() {
        val notExistedTriggerTimeEntity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(triggerTimeUtc = null, isTriggerTimeDateOnly = null))
        }
        val actualContent = ScheduleContent(notExistedTriggerTimeEntity)
        assertThat(actualContent.triggerTime, nullValue())
    }

    @Test
    fun `Given triggerTimeElements is existed, When convert to content, Then content triggerTime exist`() {
        val expectedTriggerTime = genTriggerTime()
        val existedTriggerTimeEntity: ScheduleWithDetailsEntity = with(generatedEntity) {
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
        val invalidEntity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(triggerTimeUtc = null, isTriggerTimeDateOnly = genBoolean()))
        }
        ScheduleContent(invalidEntity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given triggerTimeUtc nonnull, isTriggerTimeDateOnly null, When convert to content, Then throw Exception`() {
        val invalidEntity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(triggerTimeUtc = Clock.System.now(), isTriggerTimeDateOnly = null))
        }
        ScheduleContent(invalidEntity)
    }

    @Test
    fun `Given repeatType and repeatInterval is null, When convert to content, then content repeat is null`() {
        val notExistedRepeatEntity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(repeatType = null, repeatInterval = null))
        }
        val actualContent = ScheduleContent(notExistedRepeatEntity)
        assertThat(actualContent.repeat, nullValue())
    }

    @Test
    fun `Given repeatElements is existed, When convert to content, Then content repeat exist`() {
        val expectedRepeat = genRepeat()
        val existedTriggerTimeEntity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(
                schedule = schedule.copy(
                    repeatType = expectedRepeat.toRepeatType(),
                    repeatInterval = expectedRepeat.toIntervalAsInt(),
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
        val invalidEntity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(repeatType = null, repeatInterval = genPositiveInt().value))
        }
        ScheduleContent(invalidEntity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given repeatType nonnull, repeatInterval null, When convert to repeat, Then throw exception`() {
        val invalidEntity: ScheduleWithDetailsEntity = with(generatedEntity) {
            copy(schedule = schedule.copy(repeatType = genRepeat().toRepeatType(), repeatInterval = null))
        }
        ScheduleContent(invalidEntity)
    }
}