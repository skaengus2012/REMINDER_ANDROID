package com.nlab.reminder.core.data.model

import com.nlab.testkit.faker.genLongGreaterThanZero
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class LinkMetadataTransformsKtTest {
    @Test
    fun testLinkMetadataEntityToLinkMetadata() {
        val (_, expectedLinkMetadata, entity) = genLinkAndMetadataAndEntity()
        val actualLinkMetadata = LinkMetadata(entity)

        assertThat(actualLinkMetadata, equalTo(expectedLinkMetadata))
    }

    @Test
    fun testLinkMetadataToEntity() {
        val timestamp = genLongGreaterThanZero()
        val (link, linkMetadata, expectedEntity) = genLinkAndMetadataAndEntity(timestamp = timestamp)
        val actualEntity = linkMetadata.toLocalEntity(link, timestamp)

        assertThat(actualEntity, equalTo(expectedEntity))
    }
}