package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.local.database.dao.ScheduleContentDTO
import com.nlab.reminder.core.local.database.dao.TriggerTimeDTO
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import com.nlab.testkit.faker.genBlank
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class ScheduleTransformsKtTest {
    @Test
    fun testScheduleEntityToSchedule() {
        val (expectedSchedule, entity) = genScheduleAndEntity()
        val actualSchedule = Schedule(entity)

        assertThat(actualSchedule, equalTo(expectedSchedule))
    }

    @Test
    fun `Given that the nonBlank field is null or an empty in entity, When convert to ScheduleContent, nonBlank field is null`() {
        val (_, entity) = genScheduleAndEntity()
        assertNonBlankIsNull(ScheduleContent(entity.copyWithNonBlankField(value = null)))
        assertNonBlankIsNull(ScheduleContent(entity.copyWithNonBlankField(value = genBlank())))
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
    fun `Given that the nonBlank field is null in ScheduleContent, When convert to ScheduleContentDTO, nonBlank field is null`() {
        val (_, entity) = genScheduleAndEntity()
        assertNonBlankIsNull(ScheduleContent(entity.copyWithNonBlankField(value = null)).toLocalDTO())
        assertNonBlankIsNull(ScheduleContent(entity.copyWithNonBlankField(value = genBlank())).toLocalDTO())
    }
}

private fun ScheduleEntity.copyWithNonBlankField(value: String?) = copy(
    description = value,
    link = value
)

private fun assertNonBlankIsNull(scheduleContent: ScheduleContent) {
    assert(scheduleContent.note == null)
    assert(scheduleContent.link == null)
}

private fun assertNonBlankIsNull(scheduleContentDTO: ScheduleContentDTO) {
    assert(scheduleContentDTO.description == null)
    assert(scheduleContentDTO.link == null)
}