package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.local.database.dao.LinkMetadataSaveInput
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
    fun testLinkMetadataToSaveInput() {
        val (link, linkMetadata) = genLinkAndMetadataAndEntity()
        val expected = LinkMetadataSaveInput(
            link = link.rawLink,
            title = linkMetadata.title,
            imageUrl = linkMetadata.imageUrl
        )
        val actual = linkMetadata.toSaveInput(link)

        assertThat(actual, equalTo(expected))
    }
}