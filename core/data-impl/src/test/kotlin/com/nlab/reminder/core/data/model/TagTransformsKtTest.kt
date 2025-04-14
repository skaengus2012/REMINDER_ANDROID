package com.nlab.reminder.core.data.model

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class TagTransformsKtTest {
    @Test
    fun `Given tagEntity, When convert to Tag, Then return matching Tag`() {
        val (expectedTag, entity) = genTagAndEntity()
        val actualTag = Tag(entity)
        assertThat(actualTag, equalTo(expectedTag))
    }

    @Test
    fun `Given tagEntities, scheduleTagList, Then convert to TagUsage, Then return matching TagUsage`() {
        val tagAndEntities = genTagAndEntities()
        val tags = tagAndEntities.map { it.first }
        val tagEntities = tagAndEntities.map { it.second }
        val expectedTagUsages = genTagUsages(tags)

        val actualTagUsages = TagUsages(
            tagEntities = tagEntities.toTypedArray(),
            scheduleTagListEntities = expectedTagUsages.toScheduleTagListEntities().toTypedArray()
        )
        assertThat(actualTagUsages, equalTo(expectedTagUsages))
    }
}