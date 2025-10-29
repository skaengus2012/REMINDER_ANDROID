package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.reminder.core.local.database.transaction.ScheduleRepeatAggregate
import com.nlab.testkit.faker.genBlank
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genLong
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import kotlin.time.Instant

/**
 * @author Doohyun
 */
class ScheduleTransformsKtTest {
    @Test
    fun `Given entity and expected filled schedule, When creating schedule, Then return matched value`() {
        val (expectedSchedule, entity) = genScheduleAndEntity()
        val actualSchedule = Schedule(entity)
        assertThat(actualSchedule, equalTo(expectedSchedule))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given title is blank, When creating content, Then throw Error`() {
        val entity = buildScheduleCompositeEntity {
            copy(scheduleEntity = scheduleEntity.copy(title = genBlank()))
        }
        Schedule(entity)
    }

    @Test
    fun `Given description is null or blank, When creating content, Then content note is null`() {
        fun assertScheduleNoteNull(actual: Schedule) {
            assertThat(actual.content.note, nullValue())
        }

        val descriptionNullEntity = buildScheduleCompositeEntity {
            copy(scheduleEntity = scheduleEntity.copy(description = null))
        }
        assertScheduleNoteNull(Schedule(descriptionNullEntity))

        val descriptionBlankEntity = buildScheduleCompositeEntity {
            copy(scheduleEntity = scheduleEntity.copy(description = genBlank()))
        }
        assertScheduleNoteNull(Schedule(descriptionBlankEntity))
    }

    @Test
    fun `Given link is null or blank, When creating content, Then content link is null`() {
        fun assertScheduleLinkNull(schedule: Schedule) {
            assertThat(schedule.content.link, nullValue())
        }

        val linkNullEntity = buildScheduleCompositeEntity {
            copy(scheduleEntity = scheduleEntity.copy(link = null))
        }
        assertScheduleLinkNull(Schedule(linkNullEntity))

        val linkBlankEntity = buildScheduleCompositeEntity {
            copy(scheduleEntity = scheduleEntity.copy(link = genBlank()))
        }
        assertScheduleLinkNull(Schedule(linkBlankEntity))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given entity with negative visiblePriority, When creating schedule, Then throw exception`() {
        val invalidVisiblePriority = genLong(min = -100, max = -1)
        val entity = buildScheduleCompositeEntity {
            copy(scheduleEntity = scheduleEntity.copy(visiblePriority = invalidVisiblePriority))
        }
        Schedule(entity)
    }

    @Test
    fun `Given entity with expected visiblePriority, When creating schedule, then schedule include matched value`() {
        val expectedVisiblePriority = genNonNegativeLong().value
        val entity = buildScheduleCompositeEntity {
            copy(scheduleEntity = scheduleEntity.copy(visiblePriority = expectedVisiblePriority))
        }
        val actualSchedule = Schedule(entity)
        assertThat(actualSchedule.visiblePriority.value, equalTo(expectedVisiblePriority))
    }

    @Test
    fun `Given entity with expected complete, When creating schedule, Then schedule include matched value`() {
        val expectedComplete = genBoolean()
        val entity = buildScheduleCompositeEntity {
            copy(scheduleEntity = scheduleEntity.copy(isComplete = expectedComplete))
        }
        val actualSchedule = Schedule(entity)
        assertThat(actualSchedule.isComplete, equalTo(expectedComplete))
    }

    @Test
    fun `Given entity without trigger and repeat info, When creating Schedule, Then timing is null`() {
        val entity = buildScheduleCompositeEntity {
            copy(
                scheduleEntity = scheduleEntity.copy(
                    triggerAt = null,
                    isTriggerAtDateOnly = null,
                    repeatType = null,
                    repeatInterval = null
                ),
                repeatDetailEntities = emptySet()
            )
        }
        val actualSchedule = Schedule(entity)
        assertThat(actualSchedule.content.timing, nullValue())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given triggerAt without dateOnly flag, When creating Schedule, Then throw exception`() {
        val entity = buildScheduleCompositeEntity {
            copy(
                scheduleEntity = scheduleEntity.copy(
                    isTriggerAtDateOnly = null,
                    repeatType = null,
                    repeatInterval = null
                ),
                repeatDetailEntities = emptySet()
            )
        }
        Schedule(entity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given dateOnly flag without triggerAt, When creating Schedule, Then throw exception`() {
        val entity = buildScheduleCompositeEntity {
            copy(
                scheduleEntity = scheduleEntity.copy(
                    triggerAt = null,
                    repeatType = null,
                    repeatInterval = null
                ),
                repeatDetailEntities = emptySet()
            )
        }
        Schedule(entity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given dateOnly with hourly repeat, When creating Schedule, Then throw exception`() {
        val entity = buildScheduleCompositeEntity(
            fixtureSource = genSchedule(
                content = genScheduleContent(
                    timing = genScheduleTimingDateTimeType(repeat = genHourlyRepeat())
                )
            )
        ) {
            copy(
                scheduleEntity = scheduleEntity.copy(
                    isTriggerAtDateOnly = true
                ),
                repeatDetailEntities = emptySet()
            )
        }
        Schedule(entity)
    }

    @Test
    fun `Given triggerAt without repeat, When creating Schedule, Then timing is set and repeat is null`() {
        fun whenCreatingScheduleWithTriggerOnly(
            isTriggerAtDateOnly: Boolean,
            block: (convertedTiming: ScheduleTiming, entityTiming: Instant?) -> Unit) {
            val entity = buildScheduleCompositeEntity {
                copy(
                    scheduleEntity = scheduleEntity.copy(
                        repeatType = null,
                        repeatInterval = null,
                        isTriggerAtDateOnly = isTriggerAtDateOnly
                    ),
                    repeatDetailEntities = emptySet()
                )
            }
            val actualSchedule = Schedule(entity)
            block(actualSchedule.content.timing!!, entity.scheduleEntity.triggerAt)
        }

        whenCreatingScheduleWithTriggerOnly(isTriggerAtDateOnly = true) { convertedTiming, entityTiming ->
            convertedTiming as ScheduleTiming.Date
            assertThat(
                convertedTiming.triggerAt,
                equalTo(entityTiming)
            )
            assertThat(
                convertedTiming.dateOnlyRepeat,
                nullValue()
            )
        }
        whenCreatingScheduleWithTriggerOnly(isTriggerAtDateOnly = false) { convertedTiming, entityTiming ->
            convertedTiming as ScheduleTiming.DateTime
            assertThat(
                convertedTiming.triggerAt,
                equalTo(entityTiming)
            )
            assertThat(
                convertedTiming.repeat,
                nullValue()
            )
        }
    }

    @Test
    fun `Given date time trigger without repeat, When creating Schedule, Then timing is set and repeat is null`() {
        val entity = buildScheduleCompositeEntity {
            copy(
                scheduleEntity = scheduleEntity.copy(
                    repeatType = null,
                    repeatInterval = null,
                    isTriggerAtDateOnly = false
                ),
                repeatDetailEntities = emptySet()
            )
        }
        val actualScheduleTiming = Schedule(entity).content.timing as ScheduleTiming.DateTime
        assertThat(
            actualScheduleTiming.triggerAt,
            equalTo(entity.scheduleEntity.triggerAt)
        )
        assertThat(
            actualScheduleTiming.repeat,
            nullValue()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given repeat without trigger, When creating Schedule, Then throws exception`() {
        val entity = buildScheduleCompositeEntity {
            copy(scheduleEntity = scheduleEntity.copy(triggerAt = null, isTriggerAtDateOnly = null))
        }
        Schedule(entity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given repeat interval and details without type, When creating Schedule, Then throws exception`() {
        val entity = buildScheduleCompositeEntity {
            copy(scheduleEntity = scheduleEntity.copy(repeatType = null))
        }
        Schedule(entity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given repeat type and details without interval, When creating Schedule, Then throws exception`() {
        val entity = buildScheduleCompositeEntity {
            copy(scheduleEntity = scheduleEntity.copy(repeatInterval = null))
        }
        Schedule(entity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given repeat details without type and interval, When creating Schedule, Then throws exception`() {
        val entity = buildScheduleCompositeEntity(
            fixtureSource = genSchedule(
                content = genScheduleContent(timing = genScheduleTimingDateTimeType(repeat = genMonthlyRepeat()))
            )
        ) { copy(scheduleEntity = scheduleEntity.copy(repeatType = null, repeatInterval = null)) }
        Schedule(entity)
    }

    @Test
    fun `Given ScheduleTiming, When converting to aggregate, Then all fields are correctly mapped`() {
        fun <T : ScheduleTiming> verifyTimingToAggregate(
            timing: T,
            isTriggerAtDateOnly: Boolean,
            transformToAggregate: (T) -> ScheduleRepeatAggregate
        ) {
            val aggregate = timing.toAggregate()
            assertThat(aggregate.isTriggerAtDateOnly, equalTo(isTriggerAtDateOnly))
            assertThat(aggregate.triggerAt, equalTo(timing.triggerAt))
            assertThat(aggregate.repeat, equalTo(transformToAggregate(timing)))
        }
        verifyTimingToAggregate(
            timing = genScheduleTimingDateType(),
            isTriggerAtDateOnly = true,
            transformToAggregate = { it.dateOnlyRepeat!!.toAggregate() }
        )
        verifyTimingToAggregate(
            timing = genScheduleTimingDateTimeType(),
            isTriggerAtDateOnly = false,
            transformToAggregate = { it.repeat!!.toAggregate() }
        )
    }

    @Test
    fun `Given ScheduleTiming without repeat, When converting to aggregate, Then repeatAggregate is null`() {
        fun assertRepeatOfAggregateIsNull(timing: ScheduleTiming) {
            val aggregate = timing.toAggregate()
            assertThat(aggregate.repeat, nullValue())
        }

        assertRepeatOfAggregateIsNull(genScheduleTimingDateType(dateOnlyRepeat = null))
        assertRepeatOfAggregateIsNull(genScheduleTimingDateTimeType(repeat = null))
    }

    @Test
    fun `Given ScheduleContent, When converting to aggregate, Then all fields are correctly mapped`() {
        val scheduleContent = genScheduleContent()
        val aggregate = scheduleContent.toAggregate()

        assertThat(aggregate.headline.title, equalTo(scheduleContent.title))
        assertThat(aggregate.headline.description, equalTo(scheduleContent.note))
        assertThat(aggregate.headline.link, equalTo(scheduleContent.link!!.rawLink))
        assertThat(aggregate.timing!!, equalTo(scheduleContent.timing!!.toAggregate()))
        assertThat(aggregate.tagIds, equalTo(scheduleContent.tagIds.toSet { it.rawId }))
    }

    @Test
    fun `Given ScheduleContent without note, When converting to aggregate, Then description is null`() {
        val scheduleContent = genScheduleContent(note = null)
        val aggregate = scheduleContent.toAggregate()

        assertThat(aggregate.headline.description, nullValue())
    }

    @Test
    fun `Given ScheduleContent without link, When converting to aggregate, Then link is null`() {
        val scheduleContent = genScheduleContent(link = null)
        val aggregate = scheduleContent.toAggregate()

        assertThat(aggregate.headline.link, nullValue())
    }

    @Test
    fun `Given ScheduleContent without timing, When converting to aggregate, Then timingAggregate is null`() {
        val scheduleContent = genScheduleContent(timing = null)
        val aggregate = scheduleContent.toAggregate()

        assertThat(aggregate.timing, nullValue())
    }
}

private fun buildScheduleCompositeEntity(
    fixtureSource: Schedule = genSchedule(),
    block: ScheduleCompositeEntity.() -> ScheduleCompositeEntity
): ScheduleCompositeEntity = genScheduleAndEntity(fixtureSource).second.block()

@Suppress("TestFunctionName")
private fun Schedule(compositeEntity: ScheduleCompositeEntity): Schedule = Schedule(
    scheduleEntity = compositeEntity.scheduleEntity,
    scheduleTagListEntities = compositeEntity.scheduleTagListEntities,
    repeatDetailEntities = compositeEntity.repeatDetailEntities
)