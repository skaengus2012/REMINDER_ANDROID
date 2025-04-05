package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.local.database.model.ScheduleEntity
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class ScheduleTransformsKtTest {
    @Test
    fun testScheduleEntityToSchedule() {
        /**
        val (expectedSchedule, entity) = genScheduleAndEntity()
        val actualSchedule = Schedule(entity)

        assertThat(actualSchedule, equalTo(expectedSchedule))*/
    }

    /**
    @Test
    fun `Given that the nonBlank field is null or an empty in entity, When convert to ScheduleContent, Then nonBlank field is null`() {
        val (_, entity) = genScheduleAndEntity()
        assertNonBlankIsNull(ScheduleContent(entity.copyWithNonBlankField(value = null)))
        assertNonBlankIsNull(ScheduleContent(entity.copyWithNonBlankField(value = genBlank())))
    }

    @Test
    fun `Given triggerTime is null in entity, When convert to ScheduleContent, Then triggerTime is null`() {
        val (_, entity) = genScheduleAndEntity(schedule = genSchedule(content = genScheduleContent(triggerTime = null)))
        val content = ScheduleContent(entity)

        assertThat(content.triggerTime, nullValue())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given triggerTimeUtc and empty isTriggerTimeDateOnly in entity, When convert to ScheduleContent, Then throw Error`() {
        val (_, entity) = genScheduleAndEntity()
        ScheduleContent(entity = entity.copy(isTriggerTimeDateOnly = null))
    }

    @Test
    fun testScheduleContentToContentDTO() {
        val (schedule, entity) = genScheduleAndEntity()
        val expectedScheduleContentDTO = ScheduleContentDTO(
            title = entity.title.toNonBlankString(),
            description = entity.description!!.toNonBlankString(),
            link = entity.link!!.toNonBlankString(),
            triggerTimeDTO = TriggerTimeDTO(
                utcTime = entity.triggerTimeUtc!!,
                isDateOnly = entity.isTriggerTimeDateOnly!!
            )
        )
        val actualScheduleContentDTO = schedule.content.toLocalDTO()
        assertThat(actualScheduleContentDTO, equalTo(expectedScheduleContentDTO))
    }

    @Test
    fun `Given that the nonBlank field is null in ScheduleContent, When convert to ScheduleContentDTO, Then nonBlank field is null`() {
        val (_, entity) = genScheduleAndEntity()
        assertNonBlankIsNull(ScheduleContent(entity.copyWithNonBlankField(value = null)).toLocalDTO())
        assertNonBlankIsNull(ScheduleContent(entity.copyWithNonBlankField(value = genBlank())).toLocalDTO())
    }

    @Test
    fun `Given empty triggerTime ScheduleContent, When convert to ScheduleContentDTO, Then triggerTimeDTO is null`() {
        val (_, entity) = genScheduleAndEntity(schedule = genSchedule(content = genScheduleContent(triggerTime = null)))
        val content = ScheduleContent(entity.copyWithNonBlankField(value = null))
        val dto = content.toLocalDTO()
        assertThat(dto.triggerTimeDTO, nullValue())
    }*/
}

private fun ScheduleEntity.copyWithNonBlankField(value: String?) = copy(
    description = value,
    link = value
)

private fun assertNonBlankIsNull(scheduleContent: ScheduleContent) {
    assert(scheduleContent.note == null)
    assert(scheduleContent.link == null)
}
/**
private fun assertNonBlankIsNull(scheduleContentDTO: ScheduleContentDTO) {
    assert(scheduleContentDTO.description == null)
    assert(scheduleContentDTO.link == null)
}*/